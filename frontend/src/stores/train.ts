import { defineStore } from 'pinia'
import type { TrainOrderResponse, TrainProduct, TrainSearchPayload } from '@/api/train'

interface TrainState {
  searchParams: TrainSearchPayload
  trainsList: TrainProduct[]
  selectedTrain: TrainProduct | null
  currentOrder: TrainOrderResponse | null
}

export const useTrainStore = defineStore('train', {
  state: (): TrainState => ({
    searchParams: {
      startStation: '',
      endStation: '',
      date: '',
      trainTypes: [],
      seatTypes: []
    },
    trainsList: [],
    selectedTrain: null,
    currentOrder: null
  }),
  actions: {
    setSearchParams(params: Partial<TrainSearchPayload>) {
      this.searchParams = { ...this.searchParams, ...params }
    },
    setTrainsList(list: TrainProduct[]) {
      this.trainsList = list
    },
    setSelectedTrain(train: TrainProduct | null) {
      this.selectedTrain = train
    },
    setCurrentOrder(order: TrainOrderResponse | null) {
      this.currentOrder = order
    },
    resetOrderFlow() {
      this.selectedTrain = null
      this.currentOrder = null
    }
  }
})
