import { ReactNode } from 'react';

interface Props {
  children: ReactNode;
}

export default function DashboardLayout({ children }: Props) {
  return (
    <div className="min-h-screen bg-gray-50">
      <header className="bg-white shadow-md px-6 py-4 flex items-center">
        <h1 className="text-lg font-semibold text-gray-800">File Manager</h1>
      </header>
      <main className="p-6">{children}</main>
    </div>
  );
}
