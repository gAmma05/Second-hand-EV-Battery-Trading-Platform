import React from "react";
import { Car, Bike, BatteryFull } from "lucide-react";

const categories = [
  {
    name: "Ô tô điện",
    icon: <Car className="w-8 h-8 text-blue-500" />,
    bgColor: "bg-blue-100",
    isNew: false,
  },
  {
    name: "Xe máy điện",
    icon: <Bike className="w-8 h-8 text-green-500" />,
    bgColor: "bg-green-100",
    isNew: false,
  },
  {
    name: "Pin",
    icon: <BatteryFull className="w-8 h-8 text-yellow-500" />,
    bgColor: "bg-yellow-100",
    isNew: false,
  },
];

const CategoryGrid = () => {
  return (
    <section className="bg-gradient-to-b from-green-300 via-green-300 to-green-300 py-2 flex justify-center -mt-1">
      <div className="w-full max-w-3xl">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-8 justify-center">
          {categories.map((category) => (
            <div
              key={category.name}
              className="flex flex-col items-center p-3 rounded-2xl bg-white/10 border-2 border-white/60 hover:bg-green-100/30 hover:border-white transition-all duration-300 cursor-pointer group shadow-lg"
            >
              <div
                className={`p-2 rounded-2xl ${category.bgColor} mb-1 group-hover:scale-110 transition-transform shadow-md bg-white/80`}
              >
                {category.icon}
              </div>
              <h3 className="text-green-900 font-bold text-center text-base">
                {category.name}
              </h3>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
};

export default CategoryGrid;