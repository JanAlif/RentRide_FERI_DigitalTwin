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
import { MapScreen } from "./screens/MapScreen.jsx";
import { AdminScreen } from "./screens/AdminScreen.jsx"

//router for navigation
const router = createBrowserRouter(
  createRoutesFromElements(
    <Route path="/" element={<App />}>
      <Route index={true} path="/" element={<HeroScreen />} />
      <Route path="/login" element={<LoginScreen />} />
      <Route path="/map" element={<MapScreen />} />
      <Route path="/admin" element={<AdminScreen />} />
      {/* <Route path="/register" element={<RegisterScreen />} /> */}
      <Route path="" element={<PrivateRoute />}></Route>
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
