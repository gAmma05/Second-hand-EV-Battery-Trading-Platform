import React, { useState } from "react";
import { Heart } from "lucide-react";
import LoginModal from "../modals/LoginModal";
import RegisterModal from "../modals/RegisterModal";

const Navbar = () => {
  const [showLoginModal, setShowLoginModal] = useState(false);
  const [showRegisterModal, setShowRegisterModal] = useState(false);

  const handleOpenLogin = () => {
    setShowLoginModal(true);
    setShowRegisterModal(false);
  };

  const handleOpenRegister = () => {
    setShowRegisterModal(true);
    setShowLoginModal(false);
  };
  return (
    <header className="bg-gradient-to-br from-green-200 via-green-300 to-green-400 shadow sticky top-0 z-50">
      <div className="container mx-auto px-4 py-3 flex items-center justify-between">
        <h1 className="text-2xl md:text-3xl font-extrabold text-green-900 tracking-tight">
          EV MART
        </h1>
        <nav className="hidden md:flex space-x-8">
          {[
            "For Sellers",
            "EVs",
            "Rentals",
            "Charging",
            "Parts",
            "About",
          ].map((item) => (
            <a
              key={item}
              href="#"
              className="text-green-900 hover:text-green-600 font-semibold transition-colors duration-200 px-2 py-1 rounded"
            >
              {item}
            </a>
          ))}
        </nav>
        <div className="flex items-center space-x-3">
          <button className="text-green-900 hover:text-green-600 p-2 rounded-full transition-colors">
            <Heart className="w-6 h-6" />
          </button>
          <button 
            onClick={handleOpenLogin}
            className="text-green-700 font-bold px-4 py-2 hover:text-green-600 transition-all"
          >
            Đăng nhập
          </button>
          <button 
            onClick={handleOpenRegister}
            className="bg-white text-green-700 font-bold px-6 py-2 rounded-xl shadow hover:bg-green-100 border border-green-200 transition-all"
          >
            Đăng ký
          </button>
        </div>
      </div>

      <LoginModal 
        isOpen={showLoginModal} 
        onClose={() => setShowLoginModal(false)}
        onSwitchToRegister={handleOpenRegister}
      />

      <RegisterModal
        isOpen={showRegisterModal}
        onClose={() => setShowRegisterModal(false)}
        onSwitchToLogin={handleOpenLogin}
      />
    </header>
  );
};

export default Navbar;
