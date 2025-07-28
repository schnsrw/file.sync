import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import Login from "../pages/Login";
import Register from "../pages/Register";
import OTP from "../pages/OTP";
import Dashboard from "../pages/Dashboard";

export default function AppRouter() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Navigate to="/login" replace />} />
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route path="/verify-otp" element={<OTP />} />
        <Route path="/dashboard" element={<Dashboard />} />
      </Routes>
    </BrowserRouter>
  );
}
