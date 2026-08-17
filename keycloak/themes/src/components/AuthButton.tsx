import React from 'react'

interface AuthButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: 'primary' | 'secondary'
}

export const AuthButton: React.FC<AuthButtonProps> = ({
  type = 'button',
  disabled = false,
  variant = 'primary',
  className = '',
  children,
  ...props
}) => {
  const commonClasses = 'w-full font-medium py-3 px-4 rounded-full text-[15px] transition-colors cursor-pointer focus:outline-none focus:ring-4 disabled:cursor-not-allowed disabled:opacity-60'

  const variantClasses = {
    primary: 'bg-primary hover:bg-primary-600 active:bg-primary-700 text-white focus:ring-primary/30 disabled:bg-primary-300',
    secondary: 'bg-white hover:bg-gray-50 active:bg-gray-100 border border-[#ccd0d5] text-[#1c1e21] focus:ring-gray-200 disabled:bg-gray-50'
  }

  return (
    <button
      type={type}
      disabled={disabled}
      className={`${commonClasses} ${variantClasses[variant]} ${className}`}
      {...props}
    >
      {children}
    </button>
  )
}

export default AuthButton
