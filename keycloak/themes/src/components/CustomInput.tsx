import React, { useImperativeHandle, useRef, forwardRef } from 'react'

export interface CustomInputProps extends React.InputHTMLAttributes<HTMLInputElement> {
  id: string
  label: string
  error?: boolean
  variant?: 'classic' | 'new'
  disableFocusColor?: boolean
  icon?: React.ReactNode
}

export interface CustomInputRef {
  focus: () => void
  inputElement: HTMLInputElement | null
}

export const CustomInput = forwardRef<CustomInputRef, CustomInputProps>(({
  id,
  label,
  value = '',
  type = 'text',
  error = false,
  variant = 'new',
  disableFocusColor = false,
  icon,
  className = '',
  onChange,
  ...props
}, ref) => {
  const inputRef = useRef<HTMLInputElement | null>(null)

  useImperativeHandle(ref, () => ({
    focus: () => {
      inputRef.current?.focus()
    },
    get inputElement() {
      return inputRef.current
    }
  }))

  const hasValue = String(value).length > 0

  // Dynamic CSS classes for input
  const baseInput = 'peer block w-full border text-[15px] text-[#1c1e21] focus:outline-none transition-all placeholder-transparent'
  let inputClasses = ''

  if (variant === 'new') {
    const shape = 'bg-transparent rounded-xl px-4 pb-2.5 pt-6'
    const focusClass = disableFocusColor
      ? 'focus:border-[#ccd0d5] focus:ring-0'
      : 'focus:border-[#1877f2] focus:ring-1 focus:ring-[#1877f2]'

    const state = error
      ? 'border-[#d32f2f] focus:border-[#d32f2f] focus:ring-1 focus:ring-[#d32f2f]'
      : `border-[#ccd0d5] ${focusClass}`

    inputClasses = `${baseInput} ${shape} ${state}`
  } else {
    // Classic variant
    const shape = 'bg-[#f5f6f7] rounded-[6px] px-3 pb-2 pt-6 focus:bg-white focus:ring-2'
    const focusClass = disableFocusColor
      ? 'focus:border-[#ccd0d5] focus:ring-0'
      : 'focus:border-[#1877f2] focus:ring-[#1877f2]/20'

    const state = error
      ? 'border-[#b0281c] focus:ring-[#b0281c]/20'
      : `border-[#ccd0d5] ${focusClass}`

    inputClasses = `${baseInput} ${shape} ${state}`
  }

  // Dynamic CSS classes for label
  const baseLabel = 'absolute z-10 origin-[0] transform duration-300 cursor-text peer-focus:top-1 peer-focus:-translate-y-0 peer-focus:scale-75'
  const position = hasValue
    ? 'top-1 -translate-y-0 scale-75'
    : 'top-1/2 -translate-y-1/2 scale-100 peer-focus:top-1 peer-focus:-translate-y-0 peer-focus:scale-75'
  const labelFocusColor = disableFocusColor ? 'peer-focus:text-[#606770]' : 'peer-focus:text-[#1877f2]'

  let labelClasses = ''
  if (variant === 'new') {
    const pos = 'left-4'
    const colors = error ? 'text-[#d32f2f] peer-focus:text-[#d32f2f]' : `text-[#606770] ${labelFocusColor}`
    labelClasses = `${baseLabel} ${position} ${pos} ${colors}`
  } else {
    const pos = 'left-3'
    const colors = error ? 'text-[#b0281c] peer-focus:text-[#b0281c]' : `text-[#606770] ${labelFocusColor}`
    labelClasses = `${baseLabel} ${position} ${pos} ${colors}`
  }

  return (
    <div className={`relative w-full ${className}`}>
      <input
        id={id}
        ref={inputRef}
        type={type}
        value={value}
        onChange={onChange}
        className={inputClasses}
        placeholder=" "
        {...props}
      />

      <label htmlFor={id} className={labelClasses}>
        {label}
      </label>

      {icon && (
        <div className={`absolute top-1/2 -translate-y-1/2 flex items-center gap-2 text-[#606770] ${variant === 'new' ? 'right-4' : 'right-3'}`}>
          {icon}
        </div>
      )}
    </div>
  )
})

CustomInput.displayName = 'CustomInput'
export default CustomInput
