const SignupBanner = ({ onSignupClick, onClose }) => {
  return (
    <div className="fixed bottom-0 left-0 w-full bg-green-800 text-white flex items-center justify-between p-4 shadow-md z-50">
      <p className="text-sm md:text-lg font-medium ml-4">
        Sign up now so you don't miss the latest deals near you.
      </p>
      <div className="flex items-center gap-2 mr-4">
        <button
          onClick={onSignupClick}
          className="bg-white text-green-800 px-4 py-2 rounded-lg shadow hover:bg-gray-100 transition"
        >
          Sign Up
        </button>
        {/* Nút đóng lớn ở góc trên bên phải */}
        <button
          onClick={onClose}
          className="absolute top-0 right-2  text-white text-2xl font-bold hover:text-gray-300"
        >
          x
        </button>
      </div>
    </div>
  );
};

export default SignupBanner;
