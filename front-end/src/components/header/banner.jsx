import React from "react";
import { Search, MapPin, ChevronDown } from "lucide-react";


const Banner = () => {
  return (
  <section className="bg-gradient-to-b from-green-200 via-green-300 via-green-300 to-green-300 pb-0 pt-14">
    <div className="container mx-auto px-4 flex flex-col md:flex-row items-center">
      <div className="md:w-1/2 mb-8 md:mb-0">
        <h2 className="text-5xl md:text-6xl font-extrabold text-green-900 mb-4 leading-tight drop-shadow-lg">
          All new marketplace.<br />Explore now!
        </h2>
        {/* Box tìm kiếm */}
        <div className="bg-white rounded-2xl shadow-2xl flex flex-col md:flex-row items-center gap-3 px-4 py-4 max-w-2xl w-full">
          <button className="flex items-center px-4 py-2 text-green-700 font-semibold bg-white border border-gray-200 rounded-xl shadow hover:bg-gray-100 transition-colors">
            Browse <ChevronDown className="ml-2 w-4 h-4" />
          </button>

          <div className="flex-1 flex items-center px-4 py-2 bg-gray-50 rounded-xl border border-gray-200">
            <Search className="w-5 h-5 text-gray-400 mr-2" />
            <input
              type="text"
              placeholder="Search products..."
              className="w-full outline-none bg-transparent text-gray-700 placeholder-gray-400 text-base"
            />
          </div>

          <button className="flex items-center px-4 py-2 text-green-700 font-semibold bg-white border border-gray-200 rounded-xl shadow hover:bg-gray-100 transition-colors">
            All Regions <ChevronDown className="ml-2 w-4 h-4" />
          </button>

          <button className="bg-green-700 text-white px-8 py-2 rounded-xl font-bold shadow hover:bg-green-800 transition-all">
            Search
          </button>
        </div>
      </div>
      {/* Ảnh minh họa bên phải */}
      <div className="md:w-1/2 flex justify-center items-center mb-8 md:mb-0">
        <img
          src="/33.png"
          alt="verhicle"
          className="w-full max-w-[340px] h-auto object-contain"
          style={{ boxShadow: 'none', border: 'none' }}
        />
      </div>
    </div>
  </section>
  );
};

export default Banner;
