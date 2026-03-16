import { motion } from 'motion/react';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import PersonSearchIcon from '@mui/icons-material/PersonSearch';
import VisibilityIcon from '@mui/icons-material/Visibility';
import RadioButtonUncheckedIcon from '@mui/icons-material/RadioButtonUnchecked';

const FACE_IMAGE =
  'https://images.unsplash.com/photo-1758600434324-41712d1f530e?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxjbG9zZSUyMHVwJTIwaHVtYW4lMjBmYWNlJTIwcG9ydHJhaXQlMjBuZXV0cmFsfGVufDF8fHx8MTc3MzM3NzY3M3ww&ixlib=rb-4.1.0&q=80&w=1080';

interface EyeBox {
  id: 'left' | 'right';
  label: string;
  top: string;
  left: string;
  width: string;
  height: string;
}

const EYE_BOXES: EyeBox[] = [
  {
    id: 'left',
    label: 'Left Eye',
    top: '33%',
    left: '10%',
    width: '32%',
    height: '20%',
  },
  {
    id: 'right',
    label: 'Right Eye',
    top: '33%',
    left: '58%',
    width: '32%',
    height: '20%',
  },
];

interface EyeDetectionScreenProps {
  selectedFace: number | null;
  selectedEye: 'left' | 'right' | null;
  onSelectEye: (eye: 'left' | 'right') => void;
  onConfirm: () => void;
  onReselect: () => void;
}

const R = "'Roboto', sans-serif";

export function EyeDetectionScreen({
  selectedFace,
  selectedEye,
  onSelectEye,
  onConfirm,
  onReselect,
}: EyeDetectionScreenProps) {
  return (
    <div
      style={{
        flex: 1,
        background: '#0F1115',
        display: 'flex',
        flexDirection: 'column',
        overflow: 'hidden',
      }}
    >
      {/* Top App Bar */}
      <div
        style={{
          height: '56px',
          background: '#1C1F26',
          display: 'flex',
          alignItems: 'center',
          paddingLeft: '4px',
          paddingRight: '16px',
          flexShrink: 0,
          boxShadow: '0 1px 0 rgba(255,255,255,0.05)',
        }}
      >
        <button
          onClick={onReselect}
          style={{
            background: 'none',
            border: 'none',
            cursor: 'pointer',
            padding: '10px',
            borderRadius: '50%',
            display: 'flex',
            alignItems: 'center',
          }}
        >
          <ArrowBackIcon style={{ color: '#E6E6E6', fontSize: '24px' }} />
        </button>
        <span
          style={{
            color: '#E6E6E6',
            fontSize: '20px',
            fontWeight: 600,
            fontFamily: R,
            marginLeft: '4px',
            flex: 1,
          }}
        >
          Select Eye
        </span>
        {selectedEye !== null && (
          <motion.div
            initial={{ opacity: 0, scale: 0.8 }}
            animate={{ opacity: 1, scale: 1 }}
            style={{
              background: 'rgba(244,162,89,0.15)',
              padding: '4px 12px',
              borderRadius: '20px',
              border: '1px solid rgba(244,162,89,0.3)',
            }}
          >
            <span
              style={{
                color: '#F4A259',
                fontSize: '12px',
                fontWeight: 500,
                fontFamily: R,
              }}
            >
              {selectedEye === 'left' ? 'Left' : 'Right'} Eye selected
            </span>
          </motion.div>
        )}
      </div>

      {/* Face context info */}
      <div
        style={{
          padding: '10px 20px 8px',
          background: '#0F1115',
          flexShrink: 0,
          display: 'flex',
          alignItems: 'center',
          gap: '8px',
        }}
      >
        <div
          style={{
            background: 'rgba(244,162,89,0.1)',
            borderRadius: '8px',
            padding: '3px 10px',
            display: 'inline-flex',
            gap: '4px',
            alignItems: 'center',
          }}
        >
          <VisibilityIcon style={{ color: '#F4A259', fontSize: '14px' }} />
          <span
            style={{
              color: '#F4A259',
              fontSize: '12px',
              fontFamily: R,
              fontWeight: 500,
            }}
          >
            Face {selectedFace ?? 1}
          </span>
        </div>
        <span
          style={{
            color: '#9AA0A6',
            fontSize: '13px',
            fontFamily: R,
          }}
        >
          · Tap an eye region to select
        </span>
      </div>

      {/* Zoomed face image */}
      <div
        style={{
          flex: 1,
          padding: '0 20px',
          overflow: 'hidden',
          position: 'relative',
        }}
      >
        <div
          style={{
            width: '100%',
            height: '100%',
            position: 'relative',
            borderRadius: '16px',
            overflow: 'hidden',
            background: '#0a0a0a',
          }}
        >
          <img
            src={FACE_IMAGE}
            alt="Eye detection"
            style={{
              width: '100%',
              height: '100%',
              objectFit: 'cover',
              objectPosition: 'center 15%',
              transform: 'scale(1.55)',
              transformOrigin: 'center 25%',
              display: 'block',
            }}
          />

          {/* Dark overlay to help bounding boxes stand out */}
          <div
            style={{
              position: 'absolute',
              inset: 0,
              background: 'rgba(0,0,0,0.25)',
              pointerEvents: 'none',
            }}
          />

          {/* Eye bounding boxes */}
          {EYE_BOXES.map((box) => {
            const isSelected = selectedEye === box.id;
            return (
              <div
                key={box.id}
                onClick={() => onSelectEye(box.id)}
                style={{
                  position: 'absolute',
                  top: box.top,
                  left: box.left,
                  width: box.width,
                  height: box.height,
                  border: `2px solid ${isSelected ? '#F4A259' : 'rgba(244,162,89,0.65)'}`,
                  borderRadius: '8px',
                  cursor: 'pointer',
                  // expanded tap area
                  padding: '12px',
                  boxSizing: 'border-box',
                  transition: 'border-color 0.2s, box-shadow 0.2s',
                  boxShadow: isSelected
                    ? '0 0 0 4px rgba(244,162,89,0.25), 0 0 20px rgba(244,162,89,0.3), inset 0 0 16px rgba(244,162,89,0.1)'
                    : 'none',
                  zIndex: 10,
                  display: 'flex',
                  alignItems: 'flex-end',
                  justifyContent: 'center',
                }}
              >
                {/* Label below box */}
                <div
                  style={{
                    position: 'absolute',
                    bottom: '-26px',
                    left: '50%',
                    transform: 'translateX(-50%)',
                    background: 'rgba(0,0,0,0.80)',
                    backdropFilter: 'blur(6px)',
                    padding: '3px 10px',
                    borderRadius: '8px',
                    whiteSpace: 'nowrap',
                    display: 'flex',
                    alignItems: 'center',
                    gap: '5px',
                  }}
                >
                  <span
                    style={{
                      color: isSelected ? '#F4A259' : '#E6E6E6',
                      fontSize: '11px',
                      fontWeight: 600,
                      fontFamily: R,
                    }}
                  >
                    {box.label}
                  </span>
                </div>

                {/* Corner selection indicator */}
                <div
                  style={{
                    position: 'absolute',
                    top: '-10px',
                    right: '-10px',
                    width: '22px',
                    height: '22px',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    borderRadius: '50%',
                    background: isSelected
                      ? '#F4A259'
                      : 'rgba(20,22,28,0.85)',
                    border: isSelected
                      ? 'none'
                      : '1.5px solid rgba(244,162,89,0.5)',
                    transition: 'background 0.2s',
                  }}
                >
                  {isSelected ? (
                    <CheckCircleIcon
                      style={{ color: '#0F1115', fontSize: '22px' }}
                    />
                  ) : (
                    <RadioButtonUncheckedIcon
                      style={{ color: '#F4A259', fontSize: '18px' }}
                    />
                  )}
                </div>
              </div>
            );
          })}
        </div>
      </div>

      {/* Bottom Buttons */}
      <div
        style={{
          padding: '16px 20px 20px',
          display: 'flex',
          flexDirection: 'column',
          gap: '10px',
          flexShrink: 0,
          background: '#0F1115',
        }}
      >
        <button
          onClick={onConfirm}
          disabled={selectedEye === null}
          style={{
            width: '100%',
            height: '52px',
            background:
              selectedEye !== null
                ? 'linear-gradient(135deg, #F4A259 0%, #FFB86C 100%)'
                : '#1e2029',
            border: 'none',
            borderRadius: '16px',
            color: selectedEye !== null ? '#0F1115' : '#4a4f5a',
            fontSize: '15px',
            fontWeight: 600,
            fontFamily: R,
            cursor: selectedEye !== null ? 'pointer' : 'not-allowed',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            gap: '8px',
            transition: 'background 0.25s, color 0.25s',
            boxShadow:
              selectedEye !== null
                ? '0 4px 16px rgba(244,162,89,0.28)'
                : 'none',
          }}
        >
          <VisibilityIcon style={{ fontSize: '20px' }} />
          Confirm Eye
        </button>

        <button
          onClick={onReselect}
          style={{
            width: '100%',
            height: '52px',
            background: '#1C1F26',
            border: '1px solid rgba(255,255,255,0.07)',
            borderRadius: '16px',
            color: '#E6E6E6',
            fontSize: '15px',
            fontWeight: 500,
            fontFamily: R,
            cursor: 'pointer',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            gap: '8px',
          }}
        >
          <PersonSearchIcon style={{ fontSize: '20px', color: '#9AA0A6' }} />
          Reselect Face
        </button>
      </div>
    </div>
  );
}
