import React, { useState } from 'react';
import { FaPhoneAlt, FaRegHeart, FaShareAlt, FaMapMarkerAlt, FaCalendarAlt, FaCarSide, FaChevronLeft, FaChevronRight } from 'react-icons/fa'; // Thêm icon mũi tên

const ViewCar = () => {
  const [currentSlide, setCurrentSlide] = useState(0);

  const car = {
    id: 1,
    name: "Mercedes-Benz C-Class",
    make: "Mercedes-Benz",
    model: "C300",
    year: 2023,
    color: "Black",
    transmission: "Automatic",
    engine: "2.0L Turbo",
    mileage: "15,000 km",
    price: "850.000.000",
    description: "Cần bán gấp xe Mercedes-Benz C-Class đời 2023, màu đen sang trọng. Xe còn mới 99%, ít đi, đã bảo dưỡng định kỳ tại hãng. Động cơ mạnh mẽ, hộp số tự động mượt mà, nội thất bọc da cao cấp, trang bị nhiều tính năng an toàn và giải trí hiện đại. Cam kết xe không đâm đụng, ngập nước. Hồ sơ pháp lý rõ ràng. Giá có thể thương lượng. Vui lòng liên hệ để xem xe và lái thử tại TP.HCM.",
    images: [ // Mảng URL ảnh thật từ internet
      "https://file.hstatic.net/1000305886/file/mercedes_c_class_2023_gia_lan_banh_tai_viet_nam_2__1__ff9475459f0f4a259c238b7d722d36d4.jpg", // Ảnh ngoại thất chính
      "https://static.carmudi.vn/data/images/2022/10/24/lg/mercedes-benz-c-class-2022-noi-that.jpg", // Ảnh nội thất
      "https://img.otokvn.com/images/2023/12/12/mercedes-benz-c-class-2023-e4.jpg", // Ảnh góc khác
      "https://autodaily.vn/uploads/2022/02/c-class-2022-thiet-ke-moi-autodaily-6.jpg", // Ảnh chi tiết hơn
      "https://media.hatten-group.com/2022/03/mercedes-c-class-2022-01.jpg" // Thêm một ảnh nữa
    ],
    sellerName: "Nguyễn Văn A",
    sellerPhone: "090xxxxxx9",
    location: "Quận 1, TP. Hồ Chí Minh",
    postedDate: "Hôm nay, 10:30 AM"
  };

  const nextSlide = () => {
    setCurrentSlide((prev) => (prev === car.images.length - 1 ? 0 : prev + 1));
  };

  const prevSlide = () => {
    setCurrentSlide((prev) => (prev === 0 ? car.images.length - 1 : prev - 1));
  };

  return (
    
    <div className="bg-gradient-to-br from-gray-50 to-gray-100 min-h-screen font-sans antialiased text-gray-800"
        style={{
                backgroundImage: "url('background.png')",
                backgroundSize: "cover",
                backgroundPosition: "center",
                backgroundAttachment: "fixed",
                backgroundRepeat: "no-repeat",
                minHeight: "100vh"
            }}
    >
      <div className="max-w-7xl mx-auto py-8 px-4 sm:px-6 lg:px-8">
        <div className="bg-white rounded-2xl shadow-xl overflow-hidden md:flex md:flex-row-reverse">
          {/* Phần thông tin chi tiết */}
          <div className="md:w-1/2 p-8 lg:p-12 flex flex-col justify-between">
            <div>
              <div className="flex justify-between items-start mb-4">
                <h1 className="text-4xl lg:text-5xl font-extrabold text-gray-900 leading-tight">
                  {car.make} {car.model} {car.year}
                </h1>
                <button className="text-gray-400 hover:text-red-500 transition-all duration-300 transform hover:scale-110 p-2 rounded-full hover:bg-red-50">
                  <FaRegHeart className="w-7 h-7" />
                </button>
              </div>

              <p className="text-4xl lg:text-5xl font-extrabold text-indigo-600 mb-6">
                {car.price} VNĐ
              </p>

              <div className="flex items-center text-gray-600 text-sm mb-6 space-x-4">
                <p className="flex items-center">
                  <FaMapMarkerAlt className="mr-2 text-indigo-400" /> {car.location}
                </p>
                <p className="flex items-center">
                  <FaCalendarAlt className="mr-2 text-indigo-400" /> {car.postedDate}
                </p>
              </div>

              <div className="mb-8">
                <h2 className="text-xl font-semibold text-gray-900 mb-3 border-b-2 border-indigo-200 pb-2">Mô tả</h2>
                <p className="text-gray-700 leading-relaxed text-base">
                  {car.description}
                </p>
              </div>

              <div className="mb-8">
                <h2 className="text-xl font-semibold text-gray-900 mb-3 border-b-2 border-indigo-200 pb-2">Thông số nổi bật</h2>
                <div className="grid grid-cols-2 gap-4 text-gray-700 text-base">
                  <p className="flex items-center"><FaCarSide className="mr-2 text-indigo-500" /> <span className="font-medium">Hãng:</span> {car.make}</p>
                  <p className="flex items-center"><FaCarSide className="mr-2 text-indigo-500" /> <span className="font-medium">Mẫu:</span> {car.model}</p>
                  <p className="flex items-center"><FaCarSide className="mr-2 text-indigo-500" /> <span className="font-medium">Năm SX:</span> {car.year}</p>
                  <p className="flex items-center"><FaCarSide className="mr-2 text-indigo-500" /> <span className="font-medium">Màu sắc:</span> {car.color}</p>
                  <p className="flex items-center"><FaCarSide className="mr-2 text-indigo-500" /> <span className="font-medium">Hộp số:</span> {car.transmission}</p>
                  <p className="flex items-center"><FaCarSide className="mr-2 text-indigo-500" /> <span className="font-medium">Động cơ:</span> {car.engine}</p>
                  <p className="flex items-center"><FaCarSide className="mr-2 text-indigo-500" /> <span className="font-medium">ODO:</span> {car.mileage}</p>
                </div>
              </div>
            </div>

            <div className="mt-8 pt-6 border-t border-gray-200">
              <h2 className="text-xl font-semibold text-gray-900 mb-4">Liên hệ người bán</h2>
              <div className="flex items-center mb-4">
                <div className="w-12 h-12 bg-gray-200 rounded-full flex items-center justify-center text-xl font-bold text-gray-600 mr-4">
                  {car.sellerName.charAt(0)}
                </div>
                <div>
                  <p className="text-lg font-semibold">{car.sellerName}</p>
                  <p className="text-sm text-gray-500">Người bán cá nhân</p>
                </div>
              </div>
              <button className="flex items-center justify-center w-full bg-indigo-600 hover:bg-indigo-700 text-white font-bold py-4 px-6 rounded-xl transition duration-300 shadow-lg hover:shadow-xl transform hover:-translate-y-1">
                <FaPhoneAlt className="mr-3 text-xl" />
                <span className="text-lg tracking-wide">{car.sellerPhone}</span>
              </button>
              <p className="text-center text-sm text-gray-500 mt-3">Bấm để hiện số điện thoại</p>
            </div>
          </div>

          {/* Phần Carousel ảnh */}
          <div className="md:w-1/2 p-4 md:p-0 flex items-center justify-center bg-gray-50 relative">
            <div className="w-full relative overflow-hidden rounded-xl shadow-lg md:rounded-none md:rounded-r-2xl">
              {/* Ảnh */}
              <div
                className="flex transition-transform duration-500 ease-in-out"
                style={{ transform: `translateX(-${currentSlide * 100}%)` }}
              >
                {car.images.map((image, index) => (
                  <img
                    key={index}
                    src={image}
                    alt={`Car image ${index + 1}`}
                    className="w-full flex-shrink-0 object-cover aspect-video" // aspect-video để giữ tỉ lệ 16:9
                  />
                ))}
              </div>

              {/* Nút điều hướng */}
              <button
                onClick={prevSlide}
                className="absolute top-1/2 left-4 transform -translate-y-1/2 bg-white bg-opacity-75 p-3 rounded-full shadow-md hover:bg-opacity-100 transition-all duration-200 text-gray-700 hover:text-gray-900 focus:outline-none"
              >
                <FaChevronLeft className="w-5 h-5" />
              </button>
              <button
                onClick={nextSlide}
                className="absolute top-1/2 right-4 transform -translate-y-1/2 bg-white bg-opacity-75 p-3 rounded-full shadow-md hover:bg-opacity-100 transition-all duration-200 text-gray-700 hover:text-gray-900 focus:outline-none"
              >
                <FaChevronRight className="w-5 h-5" />
              </button>

              {/* Chấm chỉ báo (Pagination Dots) */}
              <div className="absolute bottom-4 left-0 right-0 flex justify-center space-x-2">
                {car.images.map((_, index) => (
                  <button
                    key={index}
                    onClick={() => setCurrentSlide(index)}
                    className={`h-2 w-2 rounded-full transition-all duration-300 ${
                      index === currentSlide ? 'bg-indigo-600 w-6' : 'bg-gray-300 hover:bg-gray-400'
                    }`}
                  ></button>
                ))}
              </div>
            </div>
          </div>
        </div>

        {/* Các nút hành động phụ */}
        <div className="flex justify-center space-x-4 mt-8">
            <button className="flex items-center text-gray-600 hover:text-gray-900 transition-colors py-2 px-4 rounded-lg bg-white shadow-sm hover:shadow-md">
                <FaShareAlt className="mr-2" /> Chia sẻ
            </button>
            <button className="flex items-center text-gray-600 hover:text-gray-900 transition-colors py-2 px-4 rounded-lg bg-white shadow-sm hover:shadow-md">
                Báo cáo tin
            </button>
        </div>

      </div>
    </div>
  );
};

export default ViewCar;