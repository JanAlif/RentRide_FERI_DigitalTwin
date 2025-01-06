import { configureStore } from "@reduxjs/toolkit";
import authSlice from "./slices/authSlice";
import { apiSlice } from "./slices/apiSlice";
import { trafficApi } from './slices/trafficApiSlice';
const store = configureStore({
  reducer: {
    auth: authSlice,
    [apiSlice.reducerPath]: apiSlice.reducer,
    [trafficApi.reducerPath]: trafficApi.reducer,
  },
  middleware: (getDefaultMiddleware) =>
    getDefaultMiddleware().concat(apiSlice.middleware, trafficApi.middleware),
});

export default store;
