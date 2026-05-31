import { computed, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { searchHotels } from '@/api/hotelApi'
import { useHotelSearchStore } from '@/stores/useHotelSearchStore'
import type { HotelSearchParams, HotelVO } from '@/types/hotel'

export interface HotelDisplayVO extends HotelVO {
  recommendedRoomType: string
  estimatedDistance?: number
}

export interface GeoPoint {
  city: string
  lat: number
  lng: number
}

const CITY_CENTERS: Record<string, GeoPoint> = {
  北京: { city: '北京', lat: 39.9042, lng: 116.4074 },
  上海: { city: '上海', lat: 31.2304, lng: 121.4737 },
  成都: { city: '成都', lat: 30.5728, lng: 104.0668 },
  杭州: { city: '杭州', lat: 30.2741, lng: 120.1551 },
  三亚: { city: '三亚', lat: 18.2528, lng: 109.5119 },
  西安: { city: '西安', lat: 34.3416, lng: 108.9398 }
}

const LANDMARKS: Array<{ keys: string[]; point: GeoPoint }> = [
  { keys: ['天安门'], point: { city: '北京', lat: 39.9087, lng: 116.3975 } },
  { keys: ['王府井'], point: { city: '北京', lat: 39.9154, lng: 116.4110 } },
  { keys: ['国贸', '建国门'], point: { city: '北京', lat: 39.9097, lng: 116.4619 } },
  { keys: ['后海', '什刹海'], point: { city: '北京', lat: 39.9386, lng: 116.3868 } },
  { keys: ['望京'], point: { city: '北京', lat: 39.9968, lng: 116.4810 } },
  { keys: ['前门'], point: { city: '北京', lat: 39.8992, lng: 116.3979 } },
  { keys: ['人民广场'], point: { city: '上海', lat: 31.2304, lng: 121.4737 } },
  { keys: ['外滩'], point: { city: '上海', lat: 31.2400, lng: 121.4900 } },
  { keys: ['陆家嘴'], point: { city: '上海', lat: 31.2397, lng: 121.4998 } },
  { keys: ['新天地'], point: { city: '上海', lat: 31.2190, lng: 121.4752 } },
  { keys: ['虹桥'], point: { city: '上海', lat: 31.1968, lng: 121.3260 } },
  { keys: ['静安'], point: { city: '上海', lat: 31.2297, lng: 121.4482 } },
  { keys: ['天府广场'], point: { city: '成都', lat: 30.6570, lng: 104.0650 } },
  { keys: ['太古里'], point: { city: '成都', lat: 30.6517, lng: 104.0829 } },
  { keys: ['锦江'], point: { city: '成都', lat: 30.6471, lng: 104.0800 } },
  { keys: ['宽窄巷子'], point: { city: '成都', lat: 30.6695, lng: 104.0565 } },
  { keys: ['高新'], point: { city: '成都', lat: 30.5547, lng: 104.0648 } },
  { keys: ['熊猫'], point: { city: '成都', lat: 30.7350, lng: 104.1450 } },
  { keys: ['武林'], point: { city: '杭州', lat: 30.2769, lng: 120.1636 } },
  { keys: ['西湖'], point: { city: '杭州', lat: 30.2490, lng: 120.1437 } },
  { keys: ['钱江新城'], point: { city: '杭州', lat: 30.2469, lng: 120.2194 } },
  { keys: ['灵隐'], point: { city: '杭州', lat: 30.2409, lng: 120.1026 } },
  { keys: ['运河', '小河'], point: { city: '杭州', lat: 30.3197, lng: 120.1352 } },
  { keys: ['海棠湾'], point: { city: '三亚', lat: 18.3744, lng: 109.7355 } },
  { keys: ['亚龙湾'], point: { city: '三亚', lat: 18.2297, lng: 109.6418 } },
  { keys: ['大东海'], point: { city: '三亚', lat: 18.2198, lng: 109.5267 } },
  { keys: ['三亚湾'], point: { city: '三亚', lat: 18.2814, lng: 109.4500 } },
  { keys: ['解放路'], point: { city: '三亚', lat: 18.2528, lng: 109.5119 } },
  { keys: ['钟楼'], point: { city: '西安', lat: 34.2610, lng: 108.9420 } },
  { keys: ['大唐不夜城'], point: { city: '西安', lat: 34.2063, lng: 108.9674 } },
  { keys: ['曲江'], point: { city: '西安', lat: 34.2076, lng: 108.9821 } },
  { keys: ['科技路'], point: { city: '西安', lat: 34.2332, lng: 108.8974 } },
  { keys: ['城墙'], point: { city: '西安', lat: 34.2476, lng: 108.9423 } }
]

export function useHotelSearch() {
  const store = useHotelSearchStore()
  const loading = ref(false)
  const hotels = ref<HotelDisplayVO[]>([])
  const allHotels = ref<HotelDisplayVO[]>([])
  const total = ref(0)

  const params = computed<HotelSearchParams>(() => ({
    keyword: store.destination || undefined,
    checkInDate: store.dates[0],
    checkOutDate: store.dates[1],
    adultNum: store.adultNum,
    sort: store.sortBy === 'distance_asc' ? 'rating_desc' : store.sortBy,
    page: store.page,
    size: store.size,
    starLevel: store.filters.starLevel,
    facility: store.filters.facility,
    cancelPolicy: store.filters.cancelPolicy
  }))

  const runSearch = async () => {
    loading.value = true
    try {
      if (store.sortBy === 'distance_asc' && store.destination) {
        const result = await searchHotels({
          ...params.value,
          page: 1,
          size: 200
        })
        const sorted = decorateHotels(result.records || [], store.adultNum, store.destination, true)
        allHotels.value = sorted
        total.value = sorted.length
        hotels.value = paginate(sorted, store.page, store.size)
      } else {
        const result = await searchHotels(params.value)
        const decorated = decorateHotels(result.records || [], store.adultNum)
        hotels.value = decorated
        allHotels.value = decorated
        total.value = result.total || 0
      }
    } catch (error) {
      ElMessage.error(error instanceof Error ? error.message : '酒店搜索失败')
    } finally {
      loading.value = false
    }
  }

  return {
    store,
    loading,
    hotels,
    allHotels,
    total,
    params,
    runSearch
  }
}

export function inferRecommendedRoomType(adultNum: number) {
  if (adultNum <= 1) return '推荐大床房'
  if (adultNum === 2) return '推荐大床房 / 双床房'
  if (adultNum <= 4) return '推荐家庭套房或双床房'
  return '推荐多间双床房或套房'
}

export function resolvePoint(text?: string): GeoPoint | undefined {
  if (!text) return undefined
  const matchedLandmark = LANDMARKS.find(item => item.keys.some(key => text.includes(key)))
  if (matchedLandmark) return matchedLandmark.point
  const city = Object.keys(CITY_CENTERS).find(name => text.includes(name))
  return city ? CITY_CENTERS[city] : undefined
}

export function hotelPoint(hotel: HotelVO): GeoPoint | undefined {
  const text = `${hotel.name || ''} ${hotel.address || ''}`
  const landmarkPoint = resolvePoint(text)
  if (landmarkPoint) return landmarkPoint

  const city = Object.keys(CITY_CENTERS).find(name => text.includes(name))
  if (!city) return undefined

  const seed = hashCode(`${hotel.id || ''}${hotel.name || ''}`)
  const base = CITY_CENTERS[city]
  const angle = (seed % 360) * Math.PI / 180
  const radiusKm = 3 + (seed % 1800) / 100
  return offsetPoint(base, radiusKm, angle)
}

export function distanceKm(from?: GeoPoint, to?: GeoPoint) {
  if (!from || !to) return undefined
  const rad = Math.PI / 180
  const dLat = (to.lat - from.lat) * rad
  const dLng = (to.lng - from.lng) * rad
  const lat1 = from.lat * rad
  const lat2 = to.lat * rad
  const a = Math.sin(dLat / 2) ** 2 + Math.cos(lat1) * Math.cos(lat2) * Math.sin(dLng / 2) ** 2
  return Math.round(6371 * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a)) * 10) / 10
}

export function decorateHotels(records: HotelVO[], adultNum: number, address?: string, sortByDistance = false): HotelDisplayVO[] {
  const center = resolvePoint(address)
  const decorated = records.map(hotel => ({
    ...hotel,
    recommendedRoomType: inferRecommendedRoomType(adultNum),
    estimatedDistance: distanceKm(center, hotelPoint(hotel))
  }))
  if (center || sortByDistance) {
    return decorated.sort((left, right) => (left.estimatedDistance ?? 9999) - (right.estimatedDistance ?? 9999))
  }
  return decorated
}

export function nearbyHotels(records: HotelDisplayVO[], address: string, radius = 15) {
  const center = resolvePoint(address)
  if (!center) return { center: undefined, hotels: [] as HotelDisplayVO[] }
  const hotels = records
    .map(hotel => ({
      ...hotel,
      estimatedDistance: distanceKm(center, hotelPoint(hotel))
    }))
    .filter(hotel => hotel.estimatedDistance !== undefined && hotel.estimatedDistance <= radius)
    .sort((left, right) => (left.estimatedDistance ?? 9999) - (right.estimatedDistance ?? 9999))
  return { center, hotels }
}

function paginate<T>(records: T[], page: number, size: number) {
  const safePage = page < 1 ? 1 : page
  const safeSize = size < 1 ? 9 : size
  const start = (safePage - 1) * safeSize
  return records.slice(start, start + safeSize)
}

function offsetPoint(base: GeoPoint, radiusKm: number, angle: number): GeoPoint {
  const latOffset = (radiusKm * Math.cos(angle)) / 111
  const lngOffset = (radiusKm * Math.sin(angle)) / (111 * Math.cos(base.lat * Math.PI / 180))
  return {
    city: base.city,
    lat: base.lat + latOffset,
    lng: base.lng + lngOffset
  }
}

function hashCode(value: string) {
  let hash = 0
  for (let index = 0; index < value.length; index += 1) {
    hash = ((hash << 5) - hash + value.charCodeAt(index)) | 0
  }
  return Math.abs(hash)
}
