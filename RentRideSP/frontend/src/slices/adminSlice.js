import { apiSlice } from "./apiSlice";

const ADMIN_USERS_URL = "/api/users";
const ADMIN_CARS_URL = "/api/cars";
const ADMIN_CHARGEPOINTS_URL = "/api/chargepoints";
const ADMIN_RIDES_URL = "/api/rides";

export const adminApi = apiSlice.injectEndpoints({
    endpoints: (builder) => ({
        getAllUsers: builder.query({
            query: () => ({
                url: `${ADMIN_USERS_URL}`,
                method: "GET",
            }),
        }),
        addUser: builder.mutation({
            query: (newUser) => ({
                url: `${ADMIN_USERS_URL}/register`,
                method: "POST",
                body: newUser,
            }),
        }),
        deleteUser: builder.mutation({
            query: (id) => ({
                url: `${ADMIN_USERS_URL}/${id}`,
                method: "DELETE",
            }),
        }),
        updateUser: builder.mutation({
            query: ({ id, updatedUser }) => ({
                url: `${ADMIN_USERS_URL}/${id}`,
                method: "PUT",
                body: updatedUser,
            }),
        }),

        getAllCars: builder.query({
            query: () => ({
                url: `${ADMIN_CARS_URL}`,
                method: "GET",
            }),
        }),
        addCar: builder.mutation({
            query: (newCar) => ({
                url: `${ADMIN_CARS_URL}`,
                method: "POST",
                body: newCar,
            }),
        }),
        deleteCar: builder.mutation({
            query: (id) => ({
                url: `${ADMIN_CARS_URL}/${id}`,
                method: "DELETE",
            }),
        }),
        updateCar: builder.mutation({
            query: ({ id, updatedCar }) => ({
                url: `${ADMIN_CARS_URL}/${id}`,
                method: "PUT",
                body: updatedCar,
            }),
        }),

        getAllChargePoints: builder.query({
            query: () => ({
                url: `${ADMIN_CHARGEPOINTS_URL}`,
                method: "GET",
            }),
        }),
        addChargePoint: builder.mutation({
            query: (newChargePoint) => ({
                url: `${ADMIN_CHARGEPOINTS_URL}`,
                method: "POST",
                body: newChargePoint,
            }),
        }),
        deleteChargePoint: builder.mutation({
            query: (id) => ({
                url: `${ADMIN_CHARGEPOINTS_URL}/${id}`,
                method: "DELETE",
            }),
        }),
        updateChargePoint: builder.mutation({
            query: ({ id, updatedChargePoint }) => ({
                url: `${ADMIN_CHARGEPOINTS_URL}/${id}`,
                method: "PUT",
                body: updatedChargePoint,
            }),
        }),

        getAllRides: builder.query({
            query: () => ({
                url: `${ADMIN_RIDES_URL}`,
                method: 'GET',
            }),
        }),
        addRide: builder.mutation({
            query: (newRide) => ({
                url: `${ADMIN_RIDES_URL}`,
                method: 'POST',
                body: newRide,
            }),
        }),
        deleteRide: builder.mutation({
            query: (id) => ({
                url: `${ADMIN_RIDES_URL}/${id}`,
                method: 'DELETE',
            }),
        }),
        updateRide: builder.mutation({
            query: ({ id, updatedRide }) => ({
                url: `${ADMIN_RIDES_URL}/${id}`,
                method: 'PUT',
                body: updatedRide,
            }),
        }),
    }),
});

export const { useGetAllUsersQuery,
    useAddUserMutation,
    useDeleteUserMutation,
    useUpdateUserMutation,
    useGetAllCarsQuery,
    useAddCarMutation,
    useDeleteCarMutation,
    useUpdateCarMutation,
    useGetAllChargePointsQuery,
    useAddChargePointMutation,
    useDeleteChargePointMutation,
    useUpdateChargePointMutation,
    useGetAllRidesQuery, 
    useAddRideMutation, 
    useDeleteRideMutation, 
    useUpdateRideMutation 
} = adminApi;
