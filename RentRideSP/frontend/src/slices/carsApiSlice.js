import { apiSlice } from "./apiSlice";

const CARS_URL = "/api/cars";
const RIDES_URL = "/api/rides";

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
    addRide: builder.mutation({
      query: (ride) => ({
        url: `${RIDES_URL}`,
        method: "POST",
        body: ride,
      }),
    }),

    updateCarDetails: builder.mutation({
      query: ({ id, newKm, newLocation }) => ({
        url: `${CARS_URL}/${id}/details`,
        method: "PATCH",
        body: { newKm, newLocation },
      }),
    }),
  }),
});

export const { useGetCarsQuery, useGetCarQuery, useUpdateCarStatusMutation, useAddRideMutation, useUpdateCarDetailsMutation } = carsApi;
