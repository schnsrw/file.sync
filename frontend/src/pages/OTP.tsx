import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { verifyOtp } from '../api';

export default function OTP() {
  const navigate = useNavigate();
  const [code, setCode] = useState('');
  const userId = sessionStorage.getItem('pendingUserId') ?? '';

  const handleVerify = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      const { accessToken, refreshTooken } = await verifyOtp(userId, code);
      sessionStorage.removeItem('pendingUserId');
      localStorage.setItem('token', accessToken);
      localStorage.setItem('refreshToken', refreshTooken);
      navigate('/dashboard');
    } catch (err) {
      console.error(err);
      alert('Verification failed');
    }
  };

  return (
    <div className="min-h-screen bg-white flex items-center justify-center px-4">
      <div className="w-full max-w-md bg-white shadow-xl rounded-2xl p-8 border border-gray-100">
        <h2 className="text-2xl font-bold text-center text-gray-800 mb-6">Verify OTP</h2>
        <form onSubmit={handleVerify} className="space-y-5">
          <div>
            <label htmlFor="otp" className="block text-sm font-medium text-gray-600 mb-1">
              Verification Code
            </label>
            <input
              id="otp"
              type="text"
              value={code}
              onChange={e => setCode(e.target.value)}
              required
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>
          <button
            type="submit"
            className="w-full bg-blue-600 text-white font-semibold py-2 rounded-lg hover:bg-blue-700 transition"
          >
            Verify
          </button>
        </form>
      </div>
    </div>
  );
}
