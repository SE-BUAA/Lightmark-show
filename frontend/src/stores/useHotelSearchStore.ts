import { defineStore } from 'pinia'

export interface HotelFiltersState {
  starLevel?: number
  facility?: string
  cancelPolicy?: string
}

export interface HotelSearchState {
  destination: string
  dates: [string, string] | []
  adultNum: number
  filters: HotelFiltersState
  sortBy: string
  distanceAddress: string
  mapAddress: string
  page: number
  size: number
}

const STORAGE_KEY = 'lightmark.hotel.search'

function loadState(): HotelSearchState {
  const defaults: HotelSearchState = {
    destination: '',
    dates: [],
    adultNum: 2,
    filters: {},
    sortBy: 'rating_desc',
    distanceAddress: '',
    mapAddress: '',
    page: 1,
    size: 9
  }
  try {
    const raw = sessionStorage.getItem(STORAGE_KEY)
    return raw ? { ...defaults, ...JSON.parse(raw) } : defaults
  } catch {
    return defaults
  }
}

export const useHotelSearchStore = defineStore('hotelSearch', {
  state: (): HotelSearchState => loadState(),
  actions: {
    persist() {
      sessionStorage.setItem(STORAGE_KEY, JSON.stringify(this.$state))
    },
    setSearch(payload: Partial<HotelSearchState>) {
      Object.assign(this, payload)
      this.persist()
    },
    setFilters(filters: Partial<HotelFiltersState>) {
      this.filters = { ...this.filters, ...filters }
      this.page = 1
      this.persist()
    },
    resetFilters() {
      this.filters = {}
      this.page = 1
      this.persist()
    },
    setPage(page: number) {
      this.page = page
      this.persist()
    }
  }
})
