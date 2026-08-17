import React, { useState, useRef, useEffect } from 'react'
import { ChevronDown, CheckCircle } from 'lucide-react'

export interface DropdownOption {
  id: string
  title: string
  icon?: React.ComponentType<{ size?: number; className?: string }> | string
}

interface CustomDropdownProps {
  modelValue?: string | null
  value?: string | null
  options: DropdownOption[]
  label?: string
  placeholder?: string
  disabled?: boolean
  error?: boolean
  variant?: 'classic' | 'new'
  onChange?: (value: string) => void
  'update:modelValue'?: (value: string) => void
}

export const CustomDropdown: React.FC<CustomDropdownProps> = ({
  modelValue,
  value,
  options,
  label,
  placeholder,
  disabled = false,
  error = false,
  variant = 'new',
  onChange,
  'update:modelValue': updateModelValue
}) => {
  const currentValue = value ?? modelValue
  const [isOpen, setIsOpen] = useState(false)
  const [dropdownWidth, setDropdownWidth] = useState<string>('auto')
  const triggerRef = useRef<HTMLDivElement | null>(null)
  const containerRef = useRef<HTMLDivElement | null>(null)

  const hasValidSelection = currentValue !== undefined && currentValue !== null && currentValue !== ''
  const selectedOption = hasValidSelection
    ? options.find((opt) => opt.id === currentValue) || null
    : null

  const selectableOptions = options.filter((opt) => opt.id !== '')

  useEffect(() => {
    if (triggerRef.current) {
      setDropdownWidth(`${triggerRef.current.offsetWidth}px`)
    }
  }, [isOpen])

  // Handle resize
  useEffect(() => {
    if (!triggerRef.current) return
    const resizeObserver = new ResizeObserver((entries) => {
      for (const entry of entries) {
        setDropdownWidth(`${(entry.target as HTMLElement).offsetWidth}px`)
      }
    })
    resizeObserver.observe(triggerRef.current)
    return () => {
      resizeObserver.disconnect()
    }
  }, [])

  // Close when clicking outside
  useEffect(() => {
    const handleOutsideClick = (e: MouseEvent) => {
      if (containerRef.current && !containerRef.current.contains(e.target as Node)) {
        setIsOpen(false)
      }
    }
    document.addEventListener('mousedown', handleOutsideClick)
    return () => {
      document.removeEventListener('mousedown', handleOutsideClick)
    }
  }, [])

  const handleSelect = (id: string) => {
    if (disabled) return
    onChange?.(id)
    updateModelValue?.(id)
    setIsOpen(false)
  }

  // Dynamic styles for trigger
  const baseTrigger = 'w-full relative flex items-center justify-between transition-all select-none'
  const disabledClass = disabled ? 'opacity-50 cursor-not-allowed bg-[#f5f6f7]' : 'cursor-pointer'

  let triggerClasses = ''
  if (variant === 'new') {
    const shape = 'px-4 pb-2.5 pt-6 border rounded-xl bg-transparent hover:bg-gray-50'
    const state = error
      ? 'border-[#d32f2f] ring-1 ring-[#d32f2f]'
      : 'border-[#ccd0d5] focus:border-[#1877f2] focus:ring-1 focus:ring-[#1877f2]'
    triggerClasses = `${baseTrigger} ${disabledClass} ${shape} ${state}`
  } else {
    const shape = 'px-3 pb-2 pt-6 border rounded-[6px] bg-[#f5f6f7] hover:bg-white'
    const state = error
      ? 'border-[#b0281c] ring-1 ring-[#b0281c]/20'
      : 'border-[#ccd0d5] focus:border-[#1877f2]'
    triggerClasses = `${baseTrigger} ${disabledClass} ${shape} ${state}`
  }

  const popperClasses = `absolute z-50 bg-white shadow-[0_8px_30px_rgba(0,0,0,0.12)] border border-gray-100 max-h-[350px] overflow-y-auto ${
    variant === 'new' ? 'rounded-xl py-2 mt-1' : 'rounded-xl p-2 mt-1'
  }`

  const getItemClasses = (id: string) => {
    const isSelected = currentValue === id
    const baseItem = 'flex items-center gap-3 cursor-pointer transition-colors w-full text-left'
    if (variant === 'new') {
      const shape = 'px-4 py-3'
      const colors = isSelected ? 'bg-[#444950] text-white' : 'text-[#1c1e21] hover:bg-gray-100'
      return `${baseItem} ${shape} ${colors}`
    } else {
      const shape = 'p-3 rounded-lg mb-1 last:mb-0'
      const colors = isSelected ? 'bg-[#e7f3ff] text-[#1877f2]' : 'text-[#1c1e21] hover:bg-[#f2f2f2]'
      return `${baseItem} ${shape} ${colors}`
    }
  }

  return (
    <div ref={containerRef} className="relative w-full">
      <div
        ref={triggerRef}
        className={triggerClasses}
        onClick={() => !disabled && setIsOpen(!isOpen)}
      >
        <label
          className={`absolute left-4 z-10 origin-[0] transform transition-all duration-300 pointer-events-none ${
            hasValidSelection
              ? 'top-2 scale-75'
              : 'top-1/2 -translate-y-1/2 scale-100'
          } ${error ? 'text-[#d32f2f]' : 'text-[#606770]'}`}
        >
          {label || placeholder}
        </label>

        <div className="flex items-center gap-2 mt-0.5 w-full">
          {selectedOption?.icon && hasValidSelection && (
            typeof selectedOption.icon === 'string' ? (
              <span className="text-inherit">{selectedOption.icon}</span>
            ) : (
              <selectedOption.icon className="text-inherit" size={20} />
            )
          )}
          {hasValidSelection ? (
            <span className="text-[15px] text-[#1c1e21] font-medium leading-none truncate">
              {selectedOption?.title}
            </span>
          ) : (
            <span className="text-[15px] opacity-0 pointer-events-none leading-none">
              Spacer
            </span>
          )}
        </div>

        <ChevronDown className="text-[#1c1e21] shrink-0" size={20} />
      </div>

      {isOpen && (
        <div className={popperClasses} style={{ width: dropdownWidth }}>
          {selectableOptions.map((option) => (
            <div
              key={option.id}
              className={getItemClasses(option.id)}
              onClick={() => handleSelect(option.id)}
            >
              {option.icon && (
                <div
                  className={`p-2 rounded-full flex items-center justify-center ${
                    currentValue === option.id ? 'bg-white/20' : 'bg-[#f5f6f7]'
                  }`}
                >
                  {typeof option.icon === 'string' ? (
                    <span>{option.icon}</span>
                  ) : (
                    <option.icon size={20} />
                  )}
                </div>
              )}

              <span className="text-[15px] font-semibold flex-1 truncate">{option.title}</span>

              {currentValue === option.id && variant === 'classic' && (
                <CheckCircle className="text-[#1877f2] ml-auto shrink-0" size={20} />
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  )
}

export default CustomDropdown
