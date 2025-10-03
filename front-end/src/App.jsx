import React from "react";
import HomePage from "./pages/home";
import { BrowserRouter as Router, Routes, Route, createBrowserRouter, RouterProvider } from "react-router-dom";
import ForgotPasswordPage from "./pages/forgotPass/ForgotPasswordPage";
import OTPPage from "./pages/forgotPass/Otp";
import ResetPasswordPage from "./pages/forgotPass/resetPass";
import { ToastContainer } from "react-toastify";
import {GoogleOAuthProvider} from "@react-oauth/google";
function App() {
  const router = createBrowserRouter([
    {
      path: "/",
      element: <HomePage />,
    },
    {
      path: "/forgot-password",
      element: <ForgotPasswordPage />,
    },
    {
      path: "/otp",
      element: <OTPPage />,
    },
    {
      path: "/reset-password",
      element: <ResetPasswordPage />,

    }
  ]);
  return (
    <GoogleOAuthProvider clientId="934817882257-vnikhqmiev71pr8e9iu2ij3c00dbfv1d.apps.googleusercontent.com">
      <RouterProvider router={router} />;
      <ToastContainer />
    </GoogleOAuthProvider>
  );
}

export default App;
