import React from "react";
import Modal from "./Modal";

const TermsModal = ({ isOpen, onClose }) => {
  return (
    <Modal isOpen={isOpen} onClose={onClose} title="Terms & Privacy" size="xl">
      <div className="space-y-4 text-lg text-gray-600 max-h-[100vh] overflow-y-auto p-5">
        <h2 className="font-semibold text-gray-800">1. Điều khoản sử dụng</h2>
        <p>
          Bằng cách tạo tài khoản, bạn đồng ý tuân thủ các điều khoản dịch vụ
          của chúng tôi. Vui lòng không sử dụng dịch vụ cho các mục đích vi phạm
          pháp luật hoặc gây hại đến người khác.
        </p>

        <h2 className="font-semibold text-gray-800">2. Quyền riêng tư</h2>
        <p>
          Chúng tôi cam kết bảo mật thông tin cá nhân của bạn. Dữ liệu chỉ được
          sử dụng cho mục đích cung cấp và cải thiện dịch vụ. Chúng tôi sẽ không
          chia sẻ thông tin này cho bên thứ ba mà không có sự đồng ý của bạn.
        </p>

        <h2 className="font-semibold text-gray-800">3. Cam kết người dùng</h2>
        <p>
          Người dùng cam kết cung cấp thông tin chính xác, không chia sẻ tài
          khoản với người khác và chịu trách nhiệm về mọi hoạt động trên tài
          khoản của mình.
        </p>

        <p className="text-gray-500 text-xs">
          Nếu bạn có câu hỏi liên quan đến điều khoản & bảo mật, vui lòng liên
          hệ với chúng tôi qua email:{" "}
          <span className="text-emerald-600">support@example.com</span>.
        </p>
      </div>
    </Modal>
  );
};

export default TermsModal;
