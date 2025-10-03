import React from "react";
import { Heart } from "lucide-react";

const ItemCard = () => {
  return (
    <div className="w-full mt-10 relative">
      <div className="flex justify-end items-start gap-0 pr-0">
        <div className="bg-white/10 backdrop-blur-sm border-2 border-white/60 rounded-2xl p-6 shadow-xl w-[75%] mr-0">
          <div className="grid grid-cols-1 md:grid-cols-3 lg:grid-cols-4 gap-4">
            {/* Sample Item Card - This will be repeated */}
            <div className="bg-white rounded-xl shadow-lg hover:shadow-xl transition-all duration-300 cursor-pointer scale-90">
              <div className="relative">
                <img 
                  src="/4.png" 
                  alt="Product" 
                  className="w-full h-40 object-cover rounded-t-xl"
                />
                <button className="absolute top-3 right-3 p-2 bg-white/80 rounded-full hover:bg-white transition-colors">
                  <Heart className="w-5 h-5 text-gray-600" />
                </button>
              </div>
              <div className="p-4">
                <h3 className="text-lg font-semibold text-gray-800 mb-2">Tên Sản Phẩm</h3>
                <p className="text-green-600 font-bold mb-2">47.999.000 đ</p>
                <p className="text-gray-500 text-sm mb-2">Đến 8 triệu/tháng</p>
                <div className="flex items-center justify-between">
                  <span className="text-sm text-gray-500">Tp.HCM</span>
                  <span className="text-sm text-gray-500">5 giờ trước</span>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        {/* Ảnh Pikachu bên phải */}
        <div className="w-[150px] sticky top-24">
          <img 
            src="/pica.png" 
            alt="Pica" 
            className="w-full h-auto object-contain"
          />
        </div>
      </div>
    </div>
  );
};

export default ItemCard;
