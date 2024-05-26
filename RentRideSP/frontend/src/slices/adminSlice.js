import { apiSlice } from "./apiSlice";

const ADMIN_USERS_URL = "/api/users";
const ADMIN_CARS_URL = "/api/cars";

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
    }),
});

export const { useGetAllUsersQuery, useAddUserMutation, useDeleteUserMutation, useUpdateUserMutation, useGetAllCarsQuery, useAddCarMutation, useDeleteCarMutation, useUpdateCarMutation } = adminApi;
