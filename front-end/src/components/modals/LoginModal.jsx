import React, {useState} from "react";
import Modal from "./Modal";
import {Eye, EyeOff} from "lucide-react";
import {useNavigate} from "react-router-dom";
import axios from "axios";
import {toast} from "react-toastify";
import {GoogleLogin} from "@react-oauth/google";
import {useGoogleOneTapLogin} from "@react-oauth/google";

const LoginModal = ({isOpen, onClose, onSwitchToRegister}) => {
    const [formData, setFormData] = useState({
        email: "",
        password: "",
        rememberMe: false,
    });
    const navigate = useNavigate();
    const [showPassword, setShowPassword] = useState(false);
    const [isLoading, setIsLoading] = useState(false);

    const handleGoogleSuccess = async (credentialResponse) => {
        try {
            const response = await axios.post('/api/auth/google', {
                token: credentialResponse.credential
            });
            if (response.data.token) {
                // Store the JWT token
                localStorage.setItem('token', response.data.token);
                toast.success("Google login successful!");
                onClose();
                navigate("/");
            }
        } catch (error) {
            toast.error("Google login failed. Please try again.");
            console.error('Google login error:', error);

        }
    }

    const handleChange = (e) => {
        const {name, value, type, checked} = e.target;
        setFormData((prev) => ({
            ...prev,
            [name]: type === "checkbox" ? checked : value,
        }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setIsLoading(true);

        try {
            const response = await axios.get(
                "https://68d2aeb4cc7017eec544da0a.mockapi.io/Category"
            );
            const users = response.data;
            const user = users.find(
                (u) => u.email === formData.email && u.password === formData.password
            );

            if (user) {
                toast.success("Đăng nhập thành công!");
                setTimeout(() => {
                    onClose();
                    navigate("/");
                }, 1000);
            } else {
                toast.error("Email hoặc mật khẩu không đúng.");
            }
        } catch (error) {
            toast.error("Có lỗi xảy ra, vui lòng thử lại.");
        } finally {
            setIsLoading(false);
        }
    };

    const GoogleLoginButton = () => (
        <div className="flex justify-center w-full">
            <GoogleLogin
                onSuccess={handleGoogleSuccess}
                onError={() => {
                    toast.error("Google login failed. Please try again.");
                }}
                theme="outline"
                shape="rectangular"
                size="large"
                text="continue_with"
                useOneTap
            />
        </div>
    );


    return (
        <Modal isOpen={isOpen} onClose={onClose} title="Sign In">
            <div className="flex flex-col items-center mb-6">
                <GoogleLoginButton/>
                <p className="text-gray-500 mt-4">or sign in with:</p>
            </div>


            <form onSubmit={handleSubmit} className="space-y-4">
                <div className="space-y-4">
                    <input
                        type="email"
                        id="email"
                        name="email"
                        placeholder="Email"
                        value={formData.email}
                        onChange={handleChange}
                        className="w-full px-4 py-3 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-green-300 transition-all"
                        required
                    />

                    <div className="relative">
                        <input
                            type={showPassword ? "text" : "password"}
                            id="password"
                            name="password"
                            placeholder="Password"
                            value={formData.password}
                            onChange={handleChange}
                            className="w-full px-4 py-3 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-green-300 transition-all"
                            required
                        />
                        <button
                            type="button"
                            onClick={() => setShowPassword(!showPassword)}
                            className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400 hover:text-gray-600"
                        >
                            {showPassword ? <Eye size={20}/> : <EyeOff size={20}/>}
                        </button>
                    </div>

                    <div className="flex items-center justify-between">
                        <label className="flex items-center gap-2 text-sm text-gray-600 cursor-pointer">
                            <input
                                type="checkbox"
                                name="rememberMe"
                                checked={formData.rememberMe}
                                onChange={handleChange}
                                className="rounded border-gray-300 text-green-500 focus:ring-green-300"
                            />
                            Remember password
                        </label>
                        <button
                            type="button"
                            className="text-sm text-red-500 hover:underline"
                            onClick={() => {
                                onClose();
                                navigate("/forgot-password");
                            }}
                        >
                            Forgot Password
                        </button>
                    </div>
                </div>

                <div className="space-y-4 pt-2">
                    <button
                        type="submit"
                        disabled={isLoading}
                        className="w-full bg-gradient-to-r from-green-400 to-emerald-500 text-white py-3 rounded-lg hover:from-green-500 hover:to-emerald-600 transition-all font-medium shadow-md"
                    >
                        {isLoading ? "Đang đăng nhập..." : "Sign In"}
                    </button>

                    <div className="flex items-center justify-center gap-1 text-sm">
                        <span className="text-gray-500">Don't have an account?</span>
                        <button
                            type="button"
                            onClick={onSwitchToRegister}
                            className="text-emerald-600 hover:text-emerald-700 hover:underline font-medium"
                        >
                            Sign up
                        </button>
                    </div>
                </div>
            </form>
        </Modal>
    );
};

export default LoginModal;
