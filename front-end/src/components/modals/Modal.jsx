import React from 'react';
import { X } from 'lucide-react';

const Modal = ({ isOpen, onClose, children, title }) => {
  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 backdrop-blur-[2px] bg-black/20 z-50 flex items-center justify-center p-4">
      <div className="bg-white/95 rounded-2xl shadow-lg w-full max-w-md relative overflow-hidden border-2 border-gradient-br from-green-300 via-emerald-300 to-teal-300">
        {/* Content Container */}
        <div className="relative z-10">
          <div className="flex items-center justify-between p-6">
            <h2 className="text-2xl font-semibold text-center w-full">{title}</h2>
            <button
              onClick={onClose}
              className="absolute right-4 top-4 text-gray-400 hover:text-gray-600 transition-colors"
            >
              <X size={24} />
            </button>
          </div>
          <div className="px-8 pb-8">
            {children}
          </div>
        </div>
      </div>
    </div>
  );
};

export default Modal;