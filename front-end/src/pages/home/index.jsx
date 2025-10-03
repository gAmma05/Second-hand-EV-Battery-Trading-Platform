import React, { useState } from "react";
import {
    FiHeart,
    FiUser,
    FiShoppingCart,
    FiMenu,
    FiSearch,
    FiMapPin,
    FiChevronDown
} from "react-icons/fi";
import { DownOutlined } from '@ant-design/icons';
import { Dropdown, Space, Typography } from 'antd';
import LoginModal from "../../components/modals/LoginModal";
import RegisterModal from "../../components/modals/RegisterModal";
import SignupBanner from "../../components/body/SignupBanner";

const HomePage = () => {
    const [priceRange, setPriceRange] = useState(1000);
    const [activeTab, setActiveTab] = useState("recommended");
    const [showLoginModal, setShowLoginModal] = useState(false);
    const [showRegisterModal, setShowRegisterModal] = useState(false);
    const [showSignupBanner, setShowSignupBanner] = useState(true);

    const products = [
        {
            id: 1,
            image: "https://images.unsplash.com/photo-1564013799919-ab600027ffc6",
            title: "Modern Apartment",
            price: "5.5 triệu/tháng",
            featured: true
        },
        {
            id: 2,
            image: "https://images.unsplash.com/photo-1600585154340-be6161a56a0c",
            title: "Luxury Villa",
            price: "8 triệu/tháng",
            featured: false
        },
        {
            id: 3,
            image: "https://images.unsplash.com/photo-1600596542815-ffad4c1539a9",
            title: "Cozy Studio",
            price: "4 triệu/tháng",
            featured: true
        },
        {
            id: 4,
            image: "https://images.unsplash.com/photo-1600585154526-990dced4db0d",
            title: "Family House",
            price: "10 triệu/tháng",
            featured: false
        },
        {
            id: 2,
            image: "https://images.unsplash.com/photo-1600585154340-be6161a56a0c",
            title: "Luxury Villa",
            price: "8 triệu/tháng",
            featured: false
        },
        {
            id: 3,
            image: "https://images.unsplash.com/photo-1600596542815-ffad4c1539a9",
            title: "Cozy Studio",
            price: "4 triệu/tháng",
            featured: true
        },
        {
            id: 4,
            image: "https://images.unsplash.com/photo-1600585154526-990dced4db0d",
            title: "Family House",
            price: "10 triệu/tháng",
            featured: false
        },
        {
            id: 2,
            image: "https://images.unsplash.com/photo-1600585154340-be6161a56a0c",
            title: "Luxury Villa",
            price: "8 triệu/tháng",
            featured: false
        },
        {
            id: 3,
            image: "https://images.unsplash.com/photo-1600596542815-ffad4c1539a9",
            title: "Cozy Studio",
            price: "4 triệu/tháng",
            featured: true
        },
        {
            id: 4,
            image: "https://images.unsplash.com/photo-1600585154526-990dced4db0d",
            title: "Family House",
            price: "10 triệu/tháng",
            featured: false
        },
        {
            id: 2,
            image: "https://images.unsplash.com/photo-1600585154340-be6161a56a0c",
            title: "Luxury Villa",
            price: "8 triệu/tháng",
            featured: false
        },
        {
            id: 3,
            image: "https://images.unsplash.com/photo-1600596542815-ffad4c1539a9",
            title: "Cozy Studio",
            price: "4 triệu/tháng",
            featured: true
        },
        {
            id: 4,
            image: "https://images.unsplash.com/photo-1600585154526-990dced4db0d",
            title: "Family House",
            price: "10 triệu/tháng",
            featured: false
        }
    ];
    const items = [
        {
            key: '1',
            label: 'Item 1',
        },
        {
            key: '2',
            label: 'Item 2',
        },
        {
            key: '3',
            label: 'Item 3',
        },
    ];
    const handleOpenLogin = () => {
        setShowLoginModal(true);
        setShowRegisterModal(false);
    };
    const handleOpenRegister = () => {
        setShowRegisterModal(true);
        setShowLoginModal(false);
    };

    return (
      <div
        className="overflow-x-hidden" //coi lai hoc them cai nay ve phan tranh bi croll ngang
        style={{
          backgroundImage: "url('background.png')",
          backgroundSize: "cover",
          backgroundPosition: "center",
          backgroundAttachment: "fixed",
          backgroundRepeat: "no-repeat",
          minHeight: "100vh",
        }}
      >
        {/* Top Bar */}
        <header className="relative w-full h-[200px]">
          {/* Background */}
          <img
            src="/panner.png"
            alt="Header background"
            className="w-full h-full object-cover"
          />

          {/* Overlay */}
          <div className="absolute top-0 left-0 w-full h-full grid grid-rows-3">
            {/* 1/3 trên: Nav */}
            <div className="grid grid-cols-3 items-center px-6">
              {/* Left: Logo */}
              <div className="flex items-center gap-3">
                <button className="p-2 rounded-full bg-white shadow">
                  <FiMenu className="h-6 w-6 text-green-700" />
                </button>
                <h1 className="text-xl font-bold text-green-900">ECO-SÀNH</h1>
              </div>

              {/* Center: Navigation */}
              <nav className="flex items-center justify-center gap-8 text-gray-700 font-medium">
                <button className="hover:text-green-800">Ô tô điện</button>
                <button className="hover:text-green-800">Xe máy điện</button>
                <button className="hover:text-green-800">Pin</button>
              </nav>

              {/* Right: Actions */}
              <div className="flex items-center justify-end gap-4">
                <button className="p-2 rounded-full hover:bg-white/40">
                  <FiHeart className="h-6 w-6 text-gray-700" />
                </button>
                <button
                  className="px-3 py-1 bg-white rounded-full text-sm font-medium hover:bg-gray-100"
                  onClick={handleOpenLogin}
                >
                  Đăng nhập
                </button>
                <button
                  className="px-3 py-1 bg-black text-white rounded-full text-sm font-medium hover:bg-gray-800"
                  onClick={handleOpenLogin}
                >
                  
                  Đăng tin
                </button>
                <button className="p-2 rounded-full bg-white shadow">
                  <FiUser className="h-6 w-6 text-gray-700" />
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

            {/* 1/3 giữa: Slogan */}
            <div className="flex items-center justify-center">
              <span className="text-3xl font-bold font-poppins text-white drop-shadow-xl">
                "Sống xanh – Lái xe điện – Bảo vệ môi trường"
              </span>
            </div>

            {/* 1/3 dưới: Search */}
            <div className="flex items-center justify-center px-4 py-6 mt-10">
              {/* Outer Wrapper */}
              <div className="w-full max-w-4xl bg-white rounded-xl shadow-lg p-2">
                {/* Search Bar */}
                <div className="flex w-full rounded-lg overflow-hidden">
                  {/* Search Input Div */}
                  <div className="flex-1">
                    <input
                      type="text"
                      placeholder="Tìm sản phẩm..."
                      className="w-full px-4 py-3 text-gray-600 bg-white text-sm focus:outline-none"
                    />
                  </div>

                  {/* Dropdown Select Div */}
                  <div className="ml-2">
                    <Dropdown
                      menu={{
                        items,
                        selectable: true,
                        defaultSelectedKeys: ["3"],
                      }}
                    >
                      <button className="flex items-center gap-2 bg-white border border-white px-6 py-3 rounded-lg font-medium text-gray-700 hover:bg-gray-100">
                        <span>Danh mục</span>
                        <DownOutlined className="text-gray-600" />
                      </button>
                    </Dropdown>
                  </div>

                  {/* Search Button Div */}
                  <div className="ml-2">
                    <button className="bg-green-600 hover:bg-green-700 text-white font-medium px-6 py-2 rounded-lg h-full">
                      Tìm kiếm
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </header>

        {/* Content */}
        <main className="mx-auto px-4 py-8 max-w-[1200px] w-full mt-8">
          {/* Sub Navigation */}
          <nav
            className="bg-white bg-opacity-90  rounded-lg shadow-sm"
            style={{ marginBottom: "1rem" }}
          >
            <div className="container mx-auto px-4 py-3">
              <ul className="flex space-x-8">
                <li>
                  <a
                    href="#"
                    className="text-green-600 font-medium hover:text-green-800"
                  >
                    Oto điện
                  </a>
                </li>
                <li>
                  <a
                    href="#"
                    className="text-green-600 font-medium hover:text-green-800"
                  >
                    Xe máy điện
                  </a>
                </li>
                <li>
                  <a
                    href="#"
                    className="text-green-600 font-medium hover:text-green-800"
                  >
                    Pin
                  </a>
                </li>
              </ul>
            </div>
          </nav>
          <div className="bg-white rounded-lg p-6">
            {/* Tabs */}
            <div className="flex space-x-4 mb-6">
              <button
                onClick={() => setActiveTab("recommended")}
                className={`px-4 py-2 rounded-lg ${
                  activeTab === "recommended"
                    ? "bg-green-500 text-white"
                    : "bg-gray-100"
                }`}
              >
                Dành cho bạn
              </button>
              <button
                onClick={() => setActiveTab("newest")}
                className={`px-4 py-2 rounded-lg ${
                  activeTab === "newest"
                    ? "bg-green-500 text-white"
                    : "bg-gray-100"
                }`}
              >
                Mới nhất
              </button>
            </div>

            {/* Product Grid */}
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
              {products.map((product) => (
                <div
                  key={product.id}
                  className="bg-white rounded-lg shadow-md overflow-hidden"
                >
                  <div className="relative">
                    <img
                      src={product.image}
                      alt={product.title}
                      className="w-full h-48 object-cover"
                    />
                    <button className="absolute top-2 right-2 p-2 bg-white rounded-full shadow-md hover:bg-green-50">
                      <FiHeart className="h-5 w-5 text-green-500" />
                    </button>
                    {product.featured && (
                      <span className="absolute top-2 left-2 bg-green-500 text-white px-2 py-1 rounded-md text-sm">
                        Tin nổi bật
                      </span>
                    )}
                  </div>
                  <div className="p-4">
                    <h3 className="text-lg font-semibold mb-2">
                      {product.title}
                    </h3>
                    <p className="text-green-600 font-medium">
                      {product.price}
                    </p>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </main>
        {/* Modal đăng ký */}
        <RegisterModal
          isOpen={showRegisterModal}
          onClose={() => setShowRegisterModal(false)}
          onSwitchToLogin={() => setShowLoginModal(true)}
        />
        {/* Banner chỉ hiện khi chưa đăng nhập */}
        {showSignupBanner && (
          <SignupBanner
            onSignupClick={() => setShowRegisterModal(true)}
            onClose={() => setShowSignupBanner(false)}
          />
        )}
      </div>
    );
};

export default HomePage;