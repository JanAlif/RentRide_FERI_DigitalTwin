import { apiSlice } from "./apiSlice";

const USERS_URL = "/api/users";
const RIDES_URL = "/api/rides";

export const usersApi = apiSlice.injectEndpoints({
  endpoints: (builder) => ({
    login: builder.mutation({
      query: (data) => ({
        url: `${USERS_URL}/login`,
        method: "POST",
        body: data,
      }),
    }),
    register: builder.mutation({
      query: (data) => ({
        url: `${USERS_URL}/register`,
        method: "POST",
        body: data,
      }),
    }),
  
    updateUser: builder.mutation({
      query: ({ id, ...data }) => ({
        url: `${USERS_URL}/${id}`,
        method: "PUT",
        body: data,
      }),
    }),
    updateUserPassword: builder.mutation({
      query: ({ id, oldPassword, newPassword, confirmPassword }) => ({
        url: `${USERS_URL}/${id}/password`,
        method: "PUT",
        body: { oldPassword, newPassword, confirmPassword },
      }),
    }),
    updateUserProfilePic: builder.mutation({
      query: ({ id, formData }) => ({
        url: `${USERS_URL}/${id}/profile-pic`,
        method: "POST",
        body: formData,
      }),
    }),

    getAllRides: builder.query({
      query: () => ({
        url: `${RIDES_URL}`,
        method: "GET",
      }),
    }),

    getUserById: builder.query({
      query: (id) => ({
        url: `${USERS_URL}/${id}`,
        method: "GET",
      }),
    }),
  }),
});

export const { useLoginMutation, useRegisterMutation, useUpdateUserMutation, useUpdateUserPasswordMutation, useUpdateUserProfilePicMutation, useGetAllRidesQuery, useGetUserByIdQuery } = usersApi;
