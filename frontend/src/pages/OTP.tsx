import { useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import api from '../lib/api';
import AuthLayout from '../components/AuthLayout';
import Button from '../components/Button';

export default function OTP() {
  const [params] = useSearchParams();
  const navigate = useNavigate();
  const [code, setCode] = useState('');
  const userId = params.get('userId');

  const handleVerify = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!userId) {
      alert('Missing user');
      return;
    }
    try {
      const { data } = await api.post(`/auth/${userId}/verify`, { verificationCode: code }, {
        params: { userId },
      });
      localStorage.setItem('accessToken', data.accessToken);
      localStorage.setItem('refreshToken', data.refreshTooken);
      navigate('/dashboard');
    } catch (err) {
      console.error(err);
      alert('Verification failed');
    }
  };

  return (
    <AuthLayout title="Verify OTP">
      <form onSubmit={handleVerify} className="space-y-5">
          <div>
            <label htmlFor="code" className="block text-sm font-medium text-gray-600 mb-1">
              Verification Code
            </label>
            <input
              id="code"
              type="text"
              value={code}
              onChange={e => setCode(e.target.value)}
              required
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>
          <Button type="submit" className="w-full">Verify</Button>
        </form>
    </AuthLayout>
  );
}