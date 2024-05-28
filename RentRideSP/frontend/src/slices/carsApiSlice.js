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
    getCar: builder.query({
      query: (id) => ({
        url: `${CARS_URL}/${id}`,
        method: "GET",
      }),
    }),
    updateCarStatus: builder.mutation({
      query: ({ id, inUse }) => ({
        url: `${CARS_URL}/${id}/status`,
        method: "PATCH",
        body: { inUse },
      }),
    }),
  }),
});

export const { useGetCarsQuery, useGetCarQuery, useUpdateCarStatusMutation } = carsApi;
