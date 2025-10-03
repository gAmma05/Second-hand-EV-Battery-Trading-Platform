import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";
import { toast } from "react-toastify";

// Component icon Loader
const Loader = ({ className }) => (
  <svg
    xmlns="http://www.w3.org/2000/svg"
    viewBox="0 0 24 24"
    fill="none"
    stroke="currentColor"
    strokeWidth="2"
    strokeLinecap="round"
    strokeLinejoin="round"
    className={`${className} animate-spin`}
  >
    <path d="M21 12a9 9 0 1 1-6.219-8.56" />
  </svg>
);

const ForgotPasswordPage = () => {
  const [email, setEmail] = useState("");
  const [isLoading, setIsLoading] = useState(false);

  const navigate = useNavigate();

  // Hàm xử lý submit form
 const handleSubmit = async (e) => {
   e.preventDefault();
   setIsLoading(true);

   try {
     const response = await axios.get(
       "https://68d2aeb4cc7017eec544da0a.mockapi.io/Category"
     );
     const users = response.data;

     const user = users.find((u) => u.email === email);
     if (user) {
       toast.success(`Đã gửi liên kết đặt lại mật khẩu đến ${email}`);
       setTimeout(() => {
         navigate("/otp", { state: { id: user.id, email: user.email } });
       }, 1000);
     } else {
       toast.error("Email không tồn tại trong hệ thống");
     }
   } catch (error) {
     toast.error("Có lỗi xảy ra. Vui lòng thử lại.");
   } finally {
     setIsLoading(false);
   }
 };


  return (
    <div className="min-h-screen flex items-center justify-center p-4 bg-gradient-to-br from-green-50 to-green-200">
      <div className="w-full max-w-md bg-white p-8 md:p-10 rounded-xl shadow-2xl transition-all duration-300">
        {/* Header */}
        <div className="text-center mb-8">
          <h1 className="mt-4 text-3xl font-extrabold text-gray-800">
            Quên Mật Khẩu
          </h1>
          <p className="mt-2 text-sm text-gray-500">
            Nhập email của bạn để nhận liên kết đặt lại.
          </p>
        </div>

        {/* Form */}
        <form onSubmit={handleSubmit} className="space-y-6">
          <div>
            <label
              htmlFor="email"
              className="block text-sm font-medium text-gray-700"
            >
              Địa chỉ Email
            </label>
            <input
              id="email"
              type="email"
              autoComplete="email"
              required
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              disabled={isLoading}
              placeholder="vidu@email.com"
              className="mt-1 block w-full px-4 py-2 border border-gray-300 rounded-lg shadow-sm placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-green-500 focus:border-transparent sm:text-sm"
            />
          </div>


          <button
            type="submit"
            disabled={isLoading}
            className={`w-full flex justify-center items-center py-2 px-4 rounded-lg text-base font-medium text-white bg-green-600 hover:bg-green-700 focus:outline-none focus:ring-2 focus:ring-green-500 transition duration-150 disabled:opacity-70`}
          >
            {isLoading ? (
              <>
                <Loader className="h-5 w-5 mr-3" />
                Đang Gửi...
              </>
            ) : (
              "Gửi Liên Kết Đặt Lại"
            )}
          </button>
        </form>

        <div className="mt-6 text-center">
          <button
            className="text-sm font-medium text-green-600 hover:text-green-500 transition-colors duration-150"
            onClick={() => navigate("/")}
          >
            ← Quay lại trang chủ
          </button>
        </div>
      </div>
    </div>
  );
};

export default ForgotPasswordPage;
