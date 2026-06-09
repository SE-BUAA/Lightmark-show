import { defineStore } from 'pinia'
import type { TrainOrderResponse } from '@/api/train'
import type { VacationProduct, VacationSearchPayload } from '@/api/vacation'

interface VacationState {
  searchParams: VacationSearchPayload
  vacationsList: VacationProduct[]
  selectedVacation: VacationProduct | null
  currentOrder: TrainOrderResponse | null
}

export const useVacationStore = defineStore('vacation', {
  state: (): VacationState => ({
    searchParams: {
      destination: '',
      departCity: '',
      date: '',
      tags: []
    },
    vacationsList: [],
    selectedVacation: null,
    currentOrder: null
  }),
  actions: {
    setSearchParams(params: Partial<VacationSearchPayload>) {
      this.searchParams = { ...this.searchParams, ...params }
    },
    setVacationsList(list: VacationProduct[]) {
      this.vacationsList = list
    },
    setSelectedVacation(vacation: VacationProduct | null) {
      this.selectedVacation = vacation
    },
    setCurrentOrder(order: TrainOrderResponse | null) {
      this.currentOrder = order
    }
  }
})
