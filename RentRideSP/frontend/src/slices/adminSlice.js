import { apiSlice } from "./apiSlice";

const ADMIN_USERS_URL = "/api/users";

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
    }),
});

export const { useGetAllUsersQuery, useAddUserMutation, useDeleteUserMutation, useUpdateUserMutation } = adminApi;
