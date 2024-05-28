import { apiSlice } from "./apiSlice";

const CARS_URL = "/api/cars";

export const carsApi = apiSlice.injectEndpoints({
  endpoints: (builder) => ({
    getCars: builder.query({
      query: () => ({
        url: `${CARS_URL}`,
        method: "GET",
      }),
    }),
  }),
});

export const { useGetCarsQuery } = carsApi;
