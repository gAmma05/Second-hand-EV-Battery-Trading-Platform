import React, { useState } from "react";
import Modal from "./Modal";
import { Eye, EyeOff } from "lucide-react";
import { useNavigate } from "react-router-dom";
import axios from "axios";
import { toast } from "react-toastify";
import TermsModal from "./TermsModal";

const RegisterModal = ({ isOpen, onClose, onSwitchToLogin }) => {
  const [formData, setFormData] = useState({
    email: "",
    fullName: "",
    password: "",
    confirmPassword: "",
    isOver18: false,
  });
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [showTerms, setShowTerms] = useState(false);

  const navigate = useNavigate();

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: type === "checkbox" ? checked : value,
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!formData.isOver18) {
      toast.error("Bạn phải xác nhận trên 18 tuổi để tiếp tục.");
      return;
    }

    if (formData.password !== formData.confirmPassword) {
      toast.error("Mật khẩu và xác nhận mật khẩu không khớp.");
      return;
    }

    setIsLoading(true);

    try {
      // Kiểm tra email đã tồn tại chưa
      const res = await axios.get(
        "https://68d2aeb4cc7017eec544da0a.mockapi.io/Category"
      );
      const users = res.data;
      const emailExits = users.some((u)=> u.email === formData.email);
      if(emailExits) {
        toast.error("Email đã tồn tại. Vui lòng sử dụng email khác.");
        setIsLoading(false);
        return;
      }
      // Gọi mock API để tạo user mới
      const response = await axios.post(
        "https://68d2aeb4cc7017eec544da0a.mockapi.io/Category",
        {
          email: formData.email,
          password: formData.password,
          fullName: formData.fullName,
        }
      );

      if (response.status === 201) {
        toast.success("Đăng ký thành công! Bạn có thể đăng nhập ngay.");
        setFormData({
          email: "",
          fullName: "",
          password: "",
          confirmPassword: "",
        });
        setTimeout(() => {
          onClose();
          onSwitchToLogin();
          navigate("/");
        }, 1000);
      }
    } catch (error) {
      toast.error("Có lỗi xảy ra. Vui lòng thử lại.");
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="Create your account">
      <div className="flex flex-col items-center mb-6">
        <button
          type="button"
          className="flex items-center gap-2 px-6 py-3 border-2 border-emerald-200 rounded-lg hover:bg-emerald-50 hover:border-emerald-300 transition-all"
        >
          <img src="/gg.png" alt="Google" className="w-5 h-5" />
          <span className="text-emerald-700">Continue with Google</span>
        </button>
        <p className="text-gray-500 mt-4">or sign up with:</p>
      </div>

      <form onSubmit={handleSubmit} className="space-y-4">
        <div className="space-y-4">
          <input
            type="text"
            name="fullName"
            placeholder="Full name"
            value={formData.fullName}
            onChange={handleChange}
            className="w-full px-4 py-3 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-emerald-400 transition-all"
            required
          />
          <input
            type="email"
            name="email"
            placeholder="Email"
            value={formData.email}
            onChange={handleChange}
            className="w-full px-4 py-3 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-emerald-400 transition-all"
            required
          />
          <div className="relative">
            <input
              type={showPassword ? "text" : "password"}
              name="password"
              placeholder="Password (min. 8 char)"
              value={formData.password}
              onChange={handleChange}
              className="w-full px-4 py-3 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-emerald-400 transition-all"
              required
              minLength={8}
            />
            <button
              type="button"
              onClick={() => setShowPassword(!showPassword)}
              className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400 hover:text-gray-600"
            >
              {showPassword ? <Eye size={20} /> : <EyeOff size={20} />}
            </button>
          </div>

          <div className="relative">
            <input
              type={showConfirmPassword ? "text" : "password"}
              name="confirmPassword"
              placeholder="Confirm password"
              value={formData.confirmPassword}
              onChange={handleChange}
              className="w-full px-4 py-3 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-emerald-400 transition-all"
              required
            />
            <button
              type="button"
              onClick={() => setShowConfirmPassword(!showConfirmPassword)}
              className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400 hover:text-gray-600"
            >
              {showConfirmPassword ? <Eye size={20} /> : <EyeOff size={20} />}
            </button>
          </div>
        </div>

        <div className="space-y-4 pt-2">
          <button
            type="submit"
            disabled={isLoading}
            className="w-full bg-gradient-to-r from-green-400 to-emerald-500 text-white py-3 rounded-lg hover:from-green-500 hover:to-emerald-600 transition-all font-medium shadow-md"
          >
            Create account
          </button>
          {/*  Checkbox xác nhận trên 18 tuổi */}
          <div className="flex items-center gap-2">
            <input
              type="checkbox"
              id="isOver18"
              name="isOver18"
              checked={formData.isOver18}
              onChange={handleChange}
              className="rounded border-gray-300 text-green-500 focus:ring-green-400"
            />
            <label htmlFor="isOver18" className="text-sm text-gray-600">
              Tôi xác nhận rằng tôi đã trên 18 tuổi
            </label>
          </div>
          {/* Chuyển sang modal đăng nhập */}
          <p className="text-center text-sm text-gray-500">
            Already have an account?{" "}
            <button
              type="button"
              onClick={onSwitchToLogin}
              className="text-emerald-600 hover:text-emerald-700 hover:underline font-medium"
            >
              Sign in
            </button>
          </p>
          {/* Terms & Privacy */}
          <p className="text-xs text-center text-gray-500">
            By clicking "Create account", you agree to our{" "}
            <button
              type="button"
              className="text-emerald-600 hover:underline"
              onClick={() => setShowTerms(true)}
            >
              Terms & Privacy
            </button>
            {/* Modal hiển thị Terms & Privacy */}
             <TermsModal isOpen={showTerms} onClose={() => setShowTerms(false)} />
          </p>
        </div>
      </form>
    </Modal>
  );
};

export default RegisterModal;
