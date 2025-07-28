import { ButtonHTMLAttributes } from 'react';

interface Props extends ButtonHTMLAttributes<HTMLButtonElement> {
  className?: string;
}

export default function Button({ className = '', ...props }: Props) {
  return (
    <button
      className={`bg-blue-600 text-white font-medium rounded-md px-4 py-2 hover:bg-blue-700 transition ${className}`}
      {...props}
    />
  );
}
