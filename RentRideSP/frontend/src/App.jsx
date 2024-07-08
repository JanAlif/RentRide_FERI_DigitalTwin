import { StickyNavbar } from "./components/Header.jsx";
import { Outlet } from "react-router-dom";

function App() {
  return (
    //dshha
    <>
      <StickyNavbar />
      <div className="pt-12 bg-gray-100 w-full">
        <Outlet />
      </div>
    </>
  );
}

export default App;
