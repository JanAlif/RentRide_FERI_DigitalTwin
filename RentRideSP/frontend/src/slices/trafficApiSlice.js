// frontend/src/slices/trafficApiSlice.js

import { apiSlice } from "./apiSlice";

// Define the base URL for traffics
const TRAFFICS_URL = "/api/traffics";

export const trafficApi = apiSlice.injectEndpoints({
  endpoints: (builder) => ({
    // Fetch all traffic lines
    getTraffics: builder.query({
      query: () => ({
        url: `${TRAFFICS_URL}`,
        method: "GET",
      }),
      // Optional: Provides tags for cache invalidation
      providesTags: (result) =>
        result
          ? [
              ...result.map(({ _id }) => ({ type: 'Traffics', id: _id })),
              { type: 'Traffics', id: 'LIST' },
            ]
          : [{ type: 'Traffics', id: 'LIST' }],
    }),
    
    // Optional: Fetch a single traffic line by ID
    getTraffic: builder.query({
      query: (id) => ({
        url: `${TRAFFICS_URL}/${id}`,
        method: "GET",
      }),
      providesTags: (result, error, id) => [{ type: 'Traffics', id }],
    }),
    
    // Optional: Create a new traffic line
    createTraffic: builder.mutation({
      query: (newTraffic) => ({
        url: `${TRAFFICS_URL}`,
        method: "POST",
        body: newTraffic,
      }),
      invalidatesTags: [{ type: 'Traffics', id: 'LIST' }],
    }),
    
    // Optional: Update an existing traffic line
    updateTraffic: builder.mutation({
      query: ({ id, ...updatedTraffic }) => ({
        url: `${TRAFFICS_URL}/${id}`,
        method: "PATCH",
        body: updatedTraffic,
      }),
      invalidatesTags: (result, error, { id }) => [{ type: 'Traffics', id }],
    }),
    
    // Optional: Delete a traffic line
    deleteTraffic: builder.mutation({
      query: (id) => ({
        url: `${TRAFFICS_URL}/${id}`,
        method: "DELETE",
      }),
      invalidatesTags: (result, error, id) => [{ type: 'Traffics', id }],
    }),
  }),
});

// Export hooks for usage in functional components
export const {
  useGetTrafficsQuery,
  useGetTrafficQuery,       // Optional
  useCreateTrafficMutation, // Optional
  useUpdateTrafficMutation, // Optional
  useDeleteTrafficMutation, // Optional
} = trafficApi;