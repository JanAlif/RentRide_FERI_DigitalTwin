import React from "react";
import ReactDOM from "react-dom/client";
import App from "./App.jsx";
import "./index.css";

import store from "./store";
import { Provider } from "react-redux";

//this is for Material Tailwind
import { ThemeProvider } from "@material-tailwind/react";
import {
  createBrowserRouter,
  createRoutesFromElements,
  Route,
  RouterProvider,
} from "react-router-dom";

import PrivateRoute from "./components/PrivateRoute.jsx";
import HeroScreen from "./screens/HeroScreen";
import { LoginScreen } from "./screens/LoginScreen.jsx";
import { MapScreen } from "./screens/MapScreen 2.jsx";
import { AdminScreen } from "./screens/AdminScreen.jsx";
import { RegisterScreen } from "./screens/RegisterScreen.jsx";
import { ProfileScreen } from "./screens/ProfileScreen.jsx";
import CarScreen from "./screens/CarScreen.jsx";
import { CarDetailScreen } from "./screens/CarDetailScreen.jsx";
//router for navigation
const router = createBrowserRouter(
  createRoutesFromElements(
    <Route path="/" element={<App />}>
      <Route index={true} path="/" element={<HeroScreen />} />
      <Route path="/login" element={<LoginScreen />} />
      <Route path="/register" element={<RegisterScreen />} />
      <Route path="/car" element={<CarScreen />} />
      <Route path="" element={<PrivateRoute />}>
        <Route path="/map" element={<MapScreen />} />
        <Route path="/admin" element={<AdminScreen />} />
        <Route path="/profile" element={<ProfileScreen />} />
        <Route path="/car/:id" element={<CarDetailScreen />} />
      </Route>
    </Route>
  )
);

ReactDOM.createRoot(document.getElementById("root")).render(
  <Provider store={store}>
    <React.StrictMode>
      <ThemeProvider>
        <RouterProvider router={router} />
      </ThemeProvider>
    </React.StrictMode>
  </Provider>
);
