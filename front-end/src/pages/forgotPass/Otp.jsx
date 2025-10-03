import React, { useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import axios from "axios";
import { toast } from "react-toastify";

const OTPPage = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { id, email } = location.state || {}; // lấy id, email từ state

  const [otp, setOtp] = useState("");
  const [isLoading, setIsLoading] = useState(false);

  if (!id || !email) {
    navigate("/forgot-password"); // nếu không có id/email, quay lại trang quên mật khẩu
  }

  const handleSubmit = async (e) => {
    e.preventDefault();
    setIsLoading(true);

    try {
      const response = await axios.get(
        "https://68d2aeb4cc7017eec544da0a.mockapi.io/OTP"
      );
      const users = response.data;

      const user = users.find((u) => u.id === id);
      if (!user) {
        toast.error("Người dùng không tồn tại.");
      } else if (Number(user.OTP) === Number(otp)) {
        toast.success("OTP đúng! Chuyển sang trang đổi mật khẩu...");
        setTimeout(() => {
          navigate("/reset-password", {
            state: { id: user.id, email: user.email },
          });
        }, 1000);
      } else {
        toast.error("OTP không đúng. Vui lòng thử lại.");
      }
    } catch (error) {
      toast.error("Có lỗi xảy ra. Vui lòng thử lại.");
    } finally {
      setIsLoading(false);
    }
  };


  return (
    <div className="min-h-screen flex items-center justify-center p-4 bg-green-50">
      <div className="w-full max-w-md bg-white p-8 rounded-xl shadow-lg">
        <h1 className="text-2xl font-bold text-center mb-6">Nhập OTP</h1>
        <p className="text-sm text-gray-500 text-center mb-4">
          OTP đã gửi đến email: <span className="font-medium">{email}</span>
        </p>

        <form onSubmit={handleSubmit} className="space-y-4">
          <input
            type="number"
            placeholder="Nhập OTP"
            value={otp}
            onChange={(e) => setOtp(e.target.value)}
            className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-green-500"
            required
          />

          <button
            type="submit"
            disabled={isLoading}
            className="w-full py-3 bg-green-600 text-white rounded-lg hover:bg-green-700 transition-all disabled:opacity-70"
          >
            {isLoading ? "Đang kiểm tra..." : "Xác nhận OTP"}
          </button>
        </form>
      </div>
    </div>
  );
};

export default OTPPage;
