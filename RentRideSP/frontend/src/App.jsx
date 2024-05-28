import { StickyNavbar } from "./components/Header.jsx";
import { Outlet } from "react-router-dom";

function App() {
  return (
    <>
      <StickyNavbar />
      <div className="mx-auto max-w-screen-2xl px-4 py-12">
        <Outlet />
      </div>
    </>
  );
}

export default App;
