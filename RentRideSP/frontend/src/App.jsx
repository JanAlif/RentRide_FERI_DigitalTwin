import { StickyNavbar } from "./components/Header.jsx";
import HeroScreen from "./screens/HeroScreen";
import { Outlet } from "react-router-dom";

function App() {
  return (
    <>
      <StickyNavbar />
      <div className="mx-auto max-w-screen-md py-12">
        <Outlet />
      </div>
    </>
  );
}

export default App;
