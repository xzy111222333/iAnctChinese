<template>
  <div class="map-panel">
    <div class="map-header">
      <h3 class="section-title">{{ mapTitle }}</h3>
      <div class="header-actions">
        <!-- 地理志和战记显示时间选择器 -->
        <el-select 
          v-if="type !== 'travel'" 
          v-model="selectedPeriod" 
          placeholder="选择时期" 
          size="default" 
          style="width: 200px"
        >
          <el-option label="全部时期" value="" />
          <el-option
            v-for="period in periods"
            :key="period"
            :label="period"
            :value="period"
          />
        </el-select>
        
        <el-tag v-if="!hasToken" type="warning">未配置地图 Token，显示静态示意</el-tag>
        
        <!-- 游记显示控制按钮 -->
        <div v-if="type === 'travel' && hasToken && points.length > 0" class="map-controls">
          <el-button size="small" @click="showRoute = !showRoute">
            {{ showRoute ? '隐藏路线' : '显示路线' }}
          </el-button>
          <el-button size="small" @click="animateRoute">
            <el-icon><VideoPlay /></el-icon> 动画演示
          </el-button>
          <el-button size="small" @click="fitBounds">
            <el-icon><Aim /></el-icon> 适应视图
          </el-button>
        </div>
      </div>
    </div>

    <!-- 地图容器 -->
    <div v-if="hasToken" ref="mapContainer" class="map-canvas"></div>

    <!-- 静态地图替代 -->
    <div v-else class="placeholder">
      <div class="placeholder-header">
        <p>请在 .env.local 中设置 VITE_MAPBOX_TOKEN 以启用交互式地图。</p>
        <el-button v-if="type === 'travel'" size="small" type="primary" @click="showStaticDialog = true">
          查看地点列表
        </el-button>
      </div>
      <div class="static-map">
        <!-- 游记：显示路线点 -->
        <template v-if="type === 'travel'">
          <svg class="route-line" v-if="points.length > 1">
            <path 
              :d="generateRoutePath()" 
              fill="none" 
              stroke="#c26c3d" 
              stroke-width="3" 
              stroke-dasharray="5,5"
            />
          </svg>
          <div 
            v-for="(point, index) in points" 
            :key="index"
            class="static-point"
            :style="getStaticPointStyle(index)"
            @click="selectPoint(point, index)"
          >
            <div class="point-marker">{{ point.sequence }}</div>
            <div class="point-label">{{ point.label }}</div>
          </div>
        </template>
        
        <!-- 地理志/战记：显示区域 -->
        <template v-else>
          <div 
            v-for="(item, index) in currentItems" 
            :key="index"
            class="static-region"
            :style="getStaticRegionStyle(index)"
            @click="showItemDetails(item)"
          >
            <div class="region-label">
              <span v-if="type === 'battle'">⚔</span>
              {{ item.name }}
            </div>
            <div v-if="item.period" class="region-period">{{ item.period }}</div>
          </div>
        </template>
      </div>
    </div>

    <!-- 详情弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="selectedItem?.name || selectedItem?.label"
      width="600px"
      class="point-dialog"
    >
      <div v-if="selectedItem" class="point-details">
        <!-- 游记详情 -->
        <template v-if="type === 'travel'">
          <div class="detail-row">
            <span class="label">序号：</span>
            <span class="value">第 {{ selectedItem.sequence }} 站</span>
          </div>
          <div class="detail-row">
            <span class="label">坐标：</span>
            <span class="value">
              {{ selectedItem.latitude?.toFixed(4) }}, {{ selectedItem.longitude?.toFixed(4) }}
            </span>
          </div>
          <div v-if="selectedItem.description" class="detail-row full">
            <span class="label">描述：</span>
            <p class="description">{{ selectedItem.description }}</p>
          </div>
          <div v-if="selectedItem.visitDate" class="detail-row">
            <span class="label">访问时间：</span>
            <span class="value">{{ selectedItem.visitDate }}</span>
          </div>
          <div v-if="selectedItem.stayDuration" class="detail-row">
            <span class="label">停留时长：</span>
            <span class="value">{{ selectedItem.stayDuration }}</span>
          </div>
          <div v-if="selectedItem.notes" class="detail-row full">
            <span class="label">游记摘录：</span>
            <p class="notes">{{ selectedItem.notes }}</p>
          </div>
        </template>

        <!-- 地理志详情 -->
        <template v-else-if="type === 'geography'">
          <div v-if="selectedItem.period" class="detail-row">
            <span class="label">时期：</span>
            <span class="value">{{ selectedItem.period }}</span>
          </div>
          <div v-if="selectedItem.importance" class="detail-row">
            <span class="label">重要程度：</span>
            <el-tag :type="selectedItem.importance === 'high' ? 'danger' : 'warning'">
              {{ selectedItem.importance === 'high' ? '高' : '中' }}
            </el-tag>
          </div>
          <div v-if="selectedItem.summary" class="detail-row full">
            <span class="label">事件概括：</span>
            <p class="description">{{ selectedItem.summary }}</p>
          </div>
          <div v-if="selectedItem.events?.length" class="detail-row full">
            <span class="label">重要事件：</span>
            <ul class="event-list">
              <li v-for="(event, idx) in selectedItem.events" :key="idx">
                <strong>{{ event.date }}</strong> - {{ event.description }}
              </li>
            </ul>
          </div>
          <div v-if="selectedItem.relatedFigures?.length" class="detail-row">
            <span class="label">相关人物：</span>
            <div class="value">
              <el-tag
                v-for="figure in selectedItem.relatedFigures"
                :key="figure"
                size="small"
                style="margin-right: 8px"
              >
                {{ figure }}
              </el-tag>
            </div>
          </div>
          <div v-if="selectedItem.significance" class="detail-row full">
            <span class="label">历史意义：</span>
            <p class="notes">{{ selectedItem.significance }}</p>
          </div>
        </template>

        <!-- 战记详情 -->
        <template v-else-if="type === 'battle'">
          <div v-if="selectedItem.period" class="detail-row">
            <span class="label">时期：</span>
            <span class="value">{{ selectedItem.period }}</span>
          </div>
          <div v-if="selectedItem.location" class="detail-row">
            <span class="label">战场位置：</span>
            <span class="value">{{ selectedItem.location }}</span>
          </div>
          <div v-if="selectedItem.scale" class="detail-row">
            <span class="label">战役规模：</span>
            <div class="value">
              <el-rate 
                v-model="selectedItem.scale" 
                disabled 
                show-score 
                :max="5"
                text-color="#ff9900"
              />
            </div>
          </div>
          <div v-if="selectedItem.sideA && selectedItem.sideB" class="detail-row">
            <span class="label">双方势力：</span>
            <div class="value">
              <el-tag type="danger" style="margin-right: 8px">{{ selectedItem.sideA }}</el-tag>
              <span style="margin: 0 8px">VS</span>
              <el-tag type="primary">{{ selectedItem.sideB }}</el-tag>
            </div>
          </div>
          <div v-if="selectedItem.summary" class="detail-row full">
            <span class="label">战役概括：</span>
            <p class="description">{{ selectedItem.summary }}</p>
          </div>
          <div v-if="selectedItem.outcome" class="detail-row">
            <span class="label">战果：</span>
            <div class="value">
              <el-tag :type="selectedItem.outcome.includes('胜') ? 'success' : 'danger'">
                {{ selectedItem.outcome }}
              </el-tag>
            </div>
          </div>
          <div v-if="selectedItem.commanders?.length" class="detail-row">
            <span class="label">统帅人物：</span>
            <div class="value">
              <el-tag
                v-for="commander in selectedItem.commanders"
                :key="commander"
                size="small"
                type="warning"
                style="margin-right: 8px"
              >
                {{ commander }}
              </el-tag>
            </div>
          </div>
          <div v-if="selectedItem.casualties" class="detail-row full">
            <span class="label">伤亡情况：</span>
            <p class="notes">{{ selectedItem.casualties }}</p>
          </div>
          <div v-if="selectedItem.significance" class="detail-row full">
            <span class="label">历史意义：</span>
            <p class="notes">{{ selectedItem.significance }}</p>
          </div>
        </template>
      </div>
      
      <template #footer>
        <el-button @click="prevItem" :disabled="currentIndex === 0">
          <el-icon><ArrowLeft /></el-icon> {{ type === 'travel' ? '上一站' : '上一个' }}
        </el-button>
        <el-button @click="nextItem" :disabled="currentIndex === totalItems - 1">
          {{ type === 'travel' ? '下一站' : '下一个' }} <el-icon><ArrowRight /></el-icon>
        </el-button>
        <el-button type="primary" @click="dialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 游记地点列表弹窗 -->
    <el-dialog
      v-if="type === 'travel'"
      v-model="showStaticDialog"
      title="游记地点列表"
      width="600px"
    >
      <el-timeline>
        <el-timeline-item
          v-for="(point, index) in points"
          :key="index"
          :timestamp="point.visitDate || `第 ${point.sequence} 站`"
          placement="top"
        >
          <el-card @click="selectPoint(point, index)" style="cursor: pointer">
            <h4>{{ point.label }}</h4>
            <p v-if="point.description">{{ point.description }}</p>
            <div v-if="point.stayDuration" style="font-size: 12px; color: #909399; margin-top: 8px">
              停留：{{ point.stayDuration }}
            </div>
          </el-card>
        </el-timeline-item>
      </el-timeline>
    </el-dialog>

    <!-- 游记路线信息面板 -->
    <transition name="slide-up">
      <div v-if="type === 'travel' && hasToken && showRouteInfo && points.length > 0" class="route-info-panel">
        <div class="panel-header">
          <span class="panel-title">路线信息</span>
          <el-icon class="close-icon" @click="showRouteInfo = false"><Close /></el-icon>
        </div>
        <div class="route-stats">
          <div class="stat-item">
            <div class="stat-label">总站点</div>
            <div class="stat-value">{{ points.length }} 个</div>
          </div>
          <div class="stat-item">
            <div class="stat-label">总里程</div>
            <div class="stat-value">{{ totalDistance }} km</div>
          </div>
          <div class="stat-item">
            <div class="stat-label">当前位置</div>
            <div class="stat-value">{{ currentIndex + 1 }} / {{ points.length }}</div>
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import mapboxgl from "mapbox-gl";
import { onMounted, onUnmounted, ref, computed, watch } from "vue";
import { VideoPlay, Aim, ArrowLeft, ArrowRight, Close } from '@element-plus/icons-vue';

const props = defineProps({
  type: {
    type: String,
    default: 'travel', // travel(游记), geography(地理志), battle(战记)
    validator: (value) => ['travel', 'geography', 'battle'].includes(value)
  },
  points: {
    type: Array,
    default: () => []
  },
  regions: {
    type: Array,
    default: () => []
  },
  battles: {
    type: Array,
    default: () => []
  }
});

const mapContainer = ref();
const dialogVisible = ref(false);
const showStaticDialog = ref(false);
const selectedItem = ref(null);
const currentIndex = ref(0);
const showRoute = ref(true);
const showRouteInfo = ref(true);
const animating = ref(false);
const selectedPeriod = ref('');

let map;
let markers = [];
let regionLayers = [];
let animationFrame;

const hasToken = computed(() => !!import.meta.env.VITE_MAPBOX_TOKEN);

const mapTitle = computed(() => {
  if (props.type === 'travel') return '游记轨迹';
  if (props.type === 'geography') return '地理志';
  if (props.type === 'battle') return '战记';
  return '地图';
});

const dataSource = computed(() => {
  if (props.type === 'travel') return props.points;
  if (props.type === 'geography') return props.regions;
  if (props.type === 'battle') return props.battles;
  return [];
});

const periods = computed(() => {
  if (props.type === 'travel') return [];
  const uniquePeriods = [...new Set(dataSource.value.map(item => item.period))].filter(Boolean);
  return uniquePeriods;
});

const currentItems = computed(() => {
  if (props.type === 'travel') return props.points;
  if (!selectedPeriod.value) return dataSource.value;
  return dataSource.value.filter(item => item.period === selectedPeriod.value);
});

const totalItems = computed(() => currentItems.value.length);

const totalDistance = computed(() => {
  if (props.type !== 'travel' || props.points.length < 2) return 0;
  let distance = 0;
  for (let i = 0; i < props.points.length - 1; i++) {
    const p1 = props.points[i];
    const p2 = props.points[i + 1];
    distance += calculateDistance(p1.latitude, p1.longitude, p2.latitude, p2.longitude);
  }
  return Math.round(distance);
});

const calculateDistance = (lat1, lon1, lat2, lon2) => {
  const R = 6371;
  const dLat = (lat2 - lat1) * Math.PI / 180;
  const dLon = (lon2 - lon1) * Math.PI / 180;
  const a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
    Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
    Math.sin(dLon / 2) * Math.sin(dLon / 2);
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  return R * c;
};

onMounted(() => {
  if (periods.value.length > 0 && !selectedPeriod.value) {
    selectedPeriod.value = periods.value[0];
  }

  if (!hasToken.value || currentItems.value.length === 0) return;

  mapboxgl.accessToken = import.meta.env.VITE_MAPBOX_TOKEN;
  
  const firstItem = currentItems.value[0];
  let center;
  if (props.type === 'travel') {
    center = [firstItem.longitude, firstItem.latitude];
  } else if (firstItem.bounds) {
    center = [(firstItem.bounds[0] + firstItem.bounds[2]) / 2, (firstItem.bounds[1] + firstItem.bounds[3]) / 2];
  } else {
    center = [firstItem.longitude || 0, firstItem.latitude || 0];
  }

  map = new mapboxgl.Map({
    container: mapContainer.value,
    style: "mapbox://styles/mapbox/streets-v12",
    center: center,
    zoom: 5
  });

  map.on("load", () => {
    if (props.type === 'travel') {
      initializeTravelMap();
    } else {
      initializeRegionMap();
    }
    fitBounds();
  });
});

const initializeTravelMap = () => {
  map.addSource("travel-route", {
    type: "geojson",
    data: buildGeoJson(props.points)
  });

  map.addLayer({
    id: "travel-route-line",
    type: "line",
    source: "travel-route",
    paint: {
      "line-color": "#c26c3d",
      "line-width": 4,
      "line-opacity": showRoute.value ? 0.8 : 0
    }
  });

  addTravelMarkers();
};

const initializeRegionMap = () => {
  clearRegions();
  
  currentItems.value.forEach((item, index) => {
    if (item.bounds) {
      addRegionLayer(item, index);
    } else if (item.latitude && item.longitude) {
      addPointMarker(item, index);
    }
  });
};

const addRegionLayer = (item, index) => {
  const layerId = `region-${index}`;
  regionLayers.push(layerId);

  const bounds = item.bounds;
  const coordinates = [[
    [bounds[0], bounds[1]],
    [bounds[2], bounds[1]],
    [bounds[2], bounds[3]],
    [bounds[0], bounds[3]],
    [bounds[0], bounds[1]]
  ]];

  map.addSource(layerId, {
    type: "geojson",
    data: {
      type: "Feature",
      geometry: {
        type: "Polygon",
        coordinates: coordinates
      }
    }
  });

  const fillColor = props.type === 'battle' ? "#e77761" : (item.importance === 'high' ? "#d16a5d" : "#f6c189");
  
  map.addLayer({
    id: layerId,
    type: "fill",
    source: layerId,
    paint: {
      "fill-color": fillColor,
      "fill-opacity": 0.5
    }
  });

  map.addLayer({
    id: `${layerId}-outline`,
    type: "line",
    source: layerId,
    paint: {
      "line-color": "#2f2b2a",
      "line-width": 2
    }
  });

  const centerLng = (bounds[0] + bounds[2]) / 2;
  const centerLat = (bounds[1] + bounds[3]) / 2;

  const popup = new mapboxgl.Popup({ offset: 25, closeButton: false })
    .setHTML(`
      <div style="padding: 8px">
        <h4 style="margin: 0 0 4px 0; color: #2f2b2a;">${item.name}</h4>
        ${item.period ? `<p style="margin: 0; font-size: 12px; color: #8a8178;">${item.period}</p>` : ''}
        <p style="margin: 4px 0 0 0; font-size: 12px; color: #6a645f;">点击查看详情</p>
      </div>
    `);

  const marker = new mapboxgl.Marker({ color: fillColor })
    .setLngLat([centerLng, centerLat])
    .setPopup(popup)
    .addTo(map);

  marker.getElement().addEventListener('click', () => {
    showItemDetails(item);
  });

  map.on('click', layerId, () => {
    showItemDetails(item);
  });

  map.on('mouseenter', layerId, () => {
    map.getCanvas().style.cursor = 'pointer';
  });

  map.on('mouseleave', layerId, () => {
    map.getCanvas().style.cursor = '';
  });
};

const addPointMarker = (item, index) => {
  const el = document.createElement('div');
  el.className = 'custom-battle-marker';
  el.innerHTML = `
    <div class="marker-content">
      <div class="marker-icon">⚔</div>
    </div>
  `;
  el.style.cssText = 'width: 40px; height: 40px; cursor: pointer;';

  const popup = new mapboxgl.Popup({ offset: 25, closeButton: false })
    .setHTML(`
      <div style="padding: 8px">
        <h4 style="margin: 0 0 4px 0; color: #2f2b2a;">${item.name}</h4>
        ${item.period ? `<p style="margin: 0; font-size: 12px; color: #8a8178;">${item.period}</p>` : ''}
        <p style="margin: 4px 0 0 0; font-size: 12px; color: #6a645f;">点击查看详情</p>
      </div>
    `);

  const marker = new mapboxgl.Marker({ element: el })
    .setLngLat([item.longitude, item.latitude])
    .setPopup(popup)
    .addTo(map);

  el.addEventListener('click', () => {
    showItemDetails(item);
  });

  markers.push(marker);
  addBattleMarkerStyles();
};

const addTravelMarkers = () => {
  markers.forEach(m => m.remove());
  markers = [];

  props.points.forEach((point, index) => {
    const el = document.createElement('div');
    el.className = 'custom-marker';
    el.innerHTML = `
      <div class="marker-content">
        <div class="marker-number">${point.sequence}</div>
        <div class="marker-pulse"></div>
      </div>
    `;
    el.style.cssText = 'width: 40px; height: 40px; cursor: pointer;';

    const popup = new mapboxgl.Popup({ offset: 25, closeButton: false })
      .setHTML(`
        <div style="padding: 8px">
          <h4 style="margin: 0 0 4px 0; color: #2f2b2a;">${point.label}</h4>
          <p style="margin: 0; font-size: 12px; color: #6a645f;">第 ${point.sequence} 站</p>
          ${point.description ? `<p style="margin: 4px 0 0 0; font-size: 12px;">${point.description}</p>` : ''}
        </div>
      `);

    const marker = new mapboxgl.Marker({ element: el })
      .setLngLat([point.longitude, point.latitude])
      .setPopup(popup)
      .addTo(map);

    el.addEventListener('click', () => {
      selectPoint(point, index);
    });

    markers.push(marker);
  });

  addMarkerStyles();
};

const clearRegions = () => {
  regionLayers.forEach(layerId => {
    if (map.getLayer(layerId)) map.removeLayer(layerId);
    if (map.getLayer(`${layerId}-outline`)) map.removeLayer(`${layerId}-outline`);
    if (map.getSource(layerId)) map.removeSource(layerId);
  });
  regionLayers = [];
  markers.forEach(m => m.remove());
  markers = [];
};

const addMarkerStyles = () => {
  if (document.getElementById('marker-styles')) return;
  const style = document.createElement('style');
  style.id = 'marker-styles';
  style.textContent = `
    .custom-marker .marker-content { position: relative; width: 100%; height: 100%; }
    .custom-marker .marker-number {
      position: absolute; width: 40px; height: 40px;
      background: linear-gradient(135deg, #f6c189 0%, #e77761 100%);
      border-radius: 50%; display: flex; align-items: center; justify-content: center;
      font-weight: 700; color: white; font-size: 16px;
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3); border: 3px solid white;
      z-index: 2; transition: transform 0.3s ease;
    }
    .custom-marker:hover .marker-number { transform: scale(1.15); }
    .custom-marker .marker-pulse {
      position: absolute; width: 40px; height: 40px; border-radius: 50%;
      border: 2px solid #f6c189; animation: marker-pulse 2s ease-in-out infinite; z-index: 1;
    }
    @keyframes marker-pulse {
      0%, 100% { opacity: 0; transform: scale(1); }
      50% { opacity: 0.6; transform: scale(1.4); }
    }
  `;
  document.head.appendChild(style);
};

const addBattleMarkerStyles = () => {
  if (document.getElementById('battle-marker-styles')) return;
  const style = document.createElement('style');
  style.id = 'battle-marker-styles';
  style.textContent = `
    .custom-battle-marker .marker-content { position: relative; width: 100%; height: 100%; }
    .custom-battle-marker .marker-icon {
      width: 40px; height: 40px; background: linear-gradient(135deg, #e77761, #d16a5d);
      border-radius: 50%; display: flex; align-items: center; justify-content: center;
      font-size: 20px; color: white; box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
      border: 3px solid white; transition: transform 0.3s ease;
    }
    .custom-battle-marker:hover .marker-icon { transform: scale(1.15); }
  `;
  document.head.appendChild(style);
};

const buildGeoJson = (points) => ({
  type: "FeatureCollection",
  features: [{
    type: "Feature",
    geometry: {
      type: "LineString",
      coordinates: points.map(p => [p.longitude, p.latitude])
    },
    properties: {}
  }]
});

const selectPoint = (point, index) => {
  selectedItem.value = point;
  currentIndex.value = index;
  dialogVisible.value = true;
  showStaticDialog.value = false;
  if (map) {
    map.flyTo({ center: [point.longitude, point.latitude], zoom: 10, duration: 1500 });
  }
};

const showItemDetails = (item) => {
  selectedItem.value = item;
  currentIndex.value = currentItems.value.findIndex(i => i === item);
  dialogVisible.value = true;
};

const prevItem = () => {
  if (currentIndex.value > 0) {
    currentIndex.value--;
    selectedItem.value = currentItems.value[currentIndex.value];
    if (map && selectedItem.value.longitude && selectedItem.value.latitude) {
      map.flyTo({ 
        center: [selectedItem.value.longitude, selectedItem.value.latitude], 
        zoom: 10, 
        duration: 1000 
      });
    }
  }
};

const nextItem = () => {
  if (currentIndex.value < totalItems.value - 1) {
    currentIndex.value++;
    selectedItem.value = currentItems.value[currentIndex.value];
    if (map && selectedItem.value.longitude && selectedItem.value.latitude) {
      map.flyTo({ 
        center: [selectedItem.value.longitude, selectedItem.value.latitude], 
        zoom: 10, 
        duration: 1000 
      });
    }
  }
};

const fitBounds = () => {
  if (!map || currentItems.value.length === 0) return;
  const bounds = new mapboxgl.LngLatBounds();
  
  currentItems.value.forEach(item => {
    if (item.bounds) {
      bounds.extend([item.bounds[0], item.bounds[1]]);
      bounds.extend([item.bounds[2], item.bounds[3]]);
    } else if (item.latitude && item.longitude) {
      bounds.extend([item.longitude, item.latitude]);
    }
  });

  map.fitBounds(bounds, { padding: 80, duration: 1000 });
};

const animateRoute = () => {
  if (animating.value || props.points.length < 2) return;
  animating.value = true;
  let index = 0;

  const animate = () => {
    if (index >= props.points.length) {
      animating.value = false;
      return;
    }
    const point = props.points[index];
    map.flyTo({ center: [point.longitude, point.latitude], zoom: 8, duration: 2000 });
    if (markers[index]) {
      markers[index].togglePopup();
    }
    index++;
    animationFrame = setTimeout(animate, 3000);
  };
  animate();
};

const generateRoutePath = () => {
  if (props.points.length < 2) return '';
  const path = props.points.map((point, index) => {
    const style = getStaticPointStyle(index);
    const x = parseFloat(style.left);
    const y = parseFloat(style.top);
    return `${index === 0 ? 'M' : 'L'} ${x} ${y}`;
  }).join(' ');
  return path;
};

const getStaticPointStyle = (index) => {
  const total = props.points.length;
  const left = (index / (total - 1 || 1)) * 80 + 10;
  const top = 30 + Math.sin(index * 0.5) * 20;
  return { left: `${left}%`, top: `${top}%` };
};

const getStaticRegionStyle = (index) => {
  const cols = Math.ceil(Math.sqrt(currentItems.value.length));
  const row = Math.floor(index / cols);
  const col = index % cols;
  const size = 80 / cols;
  return {
    left: `${10 + col * size}%`,
    top: `${20 + row * size}%`,
    width: `${size * 0.8}%`,
    height: `${size * 0.8}%`
  };
};

watch(showRoute, (show) => {
  if (map && map.getLayer('travel-route-line')) {
    map.setPaintProperty('travel-route-line', 'line-opacity', show ? 0.8 : 0);
  }
});

watch(selectedPeriod, () => {
  if (map && map.loaded() && props.type !== 'travel') {
    initializeRegionMap();
    fitBounds();
  }
});

watch(() => [props.points, props.regions, props.battles], () => {
  if (map && map.loaded()) {
    if (props.type === 'travel' && map.getSource("travel-route")) {
      map.getSource("travel-route").setData(buildGeoJson(props.points));
      addTravelMarkers();
    } else {
      initializeRegionMap();
    }
    fitBounds();
  }
}, { deep: true });

onUnmounted(() => {
  if (map) map.remove();
  if (animationFrame) clearTimeout(animationFrame);
});
</script>

<style scoped>
@import 'mapbox-gl/dist/mapbox-gl.css';

.map-panel { margin-top: 16px; position: relative; }
.map-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 12px; flex-wrap: wrap; gap: 12px; }
.section-title { margin: 0; color: #2f2b2a; font-weight: 700; font-size: 20px; }
.header-actions { display: flex; gap: 12px; align-items: center; flex-wrap: wrap; }
.map-controls { display: flex; gap: 8px; }
.map-canvas { width: 100%; height: 480px; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1); }
.placeholder { padding: 16px; border: 2px dashed #e5e7eb; border-radius: 12px; background: #f9fafb; }
.placeholder-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; flex-wrap: wrap; gap: 12px; }
.placeholder-header p { margin: 0; color: #6b7280; font-size: 14px; }
.static-map { position: relative; width: 100%; height: 360px; background: linear-gradient(135deg, #e8f4f8 0%, #f3e5d8 100%); border-radius: 8px; overflow: hidden; }
.route-line { position: absolute; top: 0; left: 0; width: 100%; height: 100%; pointer-events: none; }
.static-point { position: absolute; cursor: pointer; transition: transform 0.3s ease; z-index: 10; }
.static-point:hover { transform: scale(1.2); z-index: 20; }
.point-marker { width: 36px; height: 36px; background: linear-gradient(135deg, #f6c189 0%, #e77761 100%); border-radius: 50%; display: flex; align-items: center; justify-content: center; font-weight: 700; color: white; font-size: 14px; box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3); border: 3px solid white; margin: 0 auto 4px; }
.point-label { background: white; padding: 4px 8px; border-radius: 4px; font-size: 12px; white-space: nowrap; box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1); text-align: center; }
.static-region { position: absolute; background: rgba(209, 106, 93, 0.4); border: 2px solid #d16a5d; border-radius: 8px; cursor: pointer; display: flex; flex-direction: column; align-items: center; justify-content: center; transition: all 0.3s ease; padding: 8px; }
.static-region:hover { background: rgba(209, 106, 93, 0.6); transform: scale(1.05); z-index: 10; }
.region-label { color: #2f2b2a; font-weight: 600; font-size: 14px; text-align: center; }
.region-period { color: #6a645f; font-size: 11px; margin-top: 4px; text-align: center; }
.point-details { padding: 8px 0; max-height: 60vh; overflow-y: auto; }
.detail-row { display: flex; margin-bottom: 16px; font-size: 14px; }
.detail-row.full { flex-direction: column; }
.detail-row .label { font-weight: 600; color: #2f2b2a; min-width: 90px; flex-shrink: 0; }
.detail-row .value { color: #6a645f; flex: 1; display: flex; flex-wrap: wrap; align-items: center; gap: 8px; }
.description, .notes { margin: 8px 0 0 0; padding: 12px; background: #f9f7f4; border-radius: 8px; line-height: 1.6; color: #6a645f; border-left: 4px solid #a58938; }
.event-list { margin: 8px 0 0 0; padding-left: 20px; }
.event-list li { margin-bottom: 8px; color: #6a645f; line-height: 1.6; }
.event-list strong { color: #2f2b2a; }
.route-info-panel { position: absolute; bottom: 20px; left: 20px; background: rgba(255, 255, 255, 0.95); backdrop-filter: blur(10px); padding: 16px; border-radius: 12px; box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15); z-index: 10; min-width: 240px; }
.panel-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; padding-bottom: 8px; border-bottom: 1px solid #e0dbd5; }
.panel-title { font-weight: 600; color: #2f2b2a; font-size: 14px; }
.close-icon { cursor: pointer; color: #8a8178; transition: color 0.3s ease; }
.close-icon:hover { color: #2f2b2a; }
.route-stats { display: grid; grid-template-columns: repeat(3, 1fr); gap: 12px; }
.stat-item { text-align: center; }
.stat-label { font-size: 12px; color: #8a8178; margin-bottom: 4px; }
.stat-value { font-size: 16px; font-weight: 700; color: #2f2b2a; }
.slide-up-enter-active, .slide-up-leave-active { transition: all 0.3s ease; }
.slide-up-enter-from, .slide-up-leave-to { transform: translateY(20px); opacity: 0; }
</style>