import { motion } from 'motion/react';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import HomeIcon from '@mui/icons-material/Home';
import RadioButtonUncheckedIcon from '@mui/icons-material/RadioButtonUnchecked';

const FACE_IMAGE =
  'https://images.unsplash.com/photo-1758600434324-41712d1f530e?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxjbG9zZSUyMHVwJTIwaHVtYW4lMjBmYWNlJTIwcG9ydHJhaXQlMjBuZXV0cmFsfGVufDF8fHx8MTc3MzM3NzY3M3ww&ixlib=rb-4.1.0&q=80&w=1080';

interface FaceBox {
  id: number;
  label: string;
  confidence: number;
  top: string;
  left: string;
  width: string;
  height: string;
}

const FACE_BOXES: FaceBox[] = [
  {
    id: 1,
    label: 'Face 1',
    confidence: 0.97,
    top: '6%',
    left: '18%',
    width: '58%',
    height: '74%',
  },
  {
    id: 2,
    label: 'Face 2',
    confidence: 0.81,
    top: '18%',
    left: '62%',
    width: '28%',
    height: '38%',
  },
];

interface FaceDetectionScreenProps {
  selectedFace: number | null;
  onSelectFace: (face: number) => void;
  onConfirm: () => void;
  onBack: () => void;
}

const R = "'Roboto', sans-serif";

export function FaceDetectionScreen({
  selectedFace,
  onSelectFace,
  onConfirm,
  onBack,
}: FaceDetectionScreenProps) {
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
          onClick={onBack}
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
          Select Face
        </span>
        {selectedFace !== null && (
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
              Face {selectedFace} selected
            </span>
          </motion.div>
        )}
      </div>

      {/* Instruction bar */}
      <div
        style={{
          padding: '10px 20px 8px',
          background: '#0F1115',
          flexShrink: 0,
        }}
      >
        <span
          style={{
            color: '#9AA0A6',
            fontSize: '13px',
            fontFamily: R,
          }}
        >
          {FACE_BOXES.length} faces detected — tap to select
        </span>
      </div>

      {/* Image area */}
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
            alt="Face detection"
            style={{
              width: '100%',
              height: '100%',
              objectFit: 'cover',
              objectPosition: 'center top',
              display: 'block',
            }}
          />

          {/* Dim unselected areas when a face is chosen */}
          {selectedFace !== null && (
            <div
              style={{
                position: 'absolute',
                inset: 0,
                background: 'rgba(0,0,0,0.3)',
                pointerEvents: 'none',
              }}
            />
          )}

          {/* Bounding boxes */}
          {FACE_BOXES.map((box) => {
            const isSelected = selectedFace === box.id;
            return (
              <div
                key={box.id}
                onClick={() => onSelectFace(box.id)}
                style={{
                  position: 'absolute',
                  top: box.top,
                  left: box.left,
                  width: box.width,
                  height: box.height,
                  border: `2px solid ${isSelected ? '#F4A259' : 'rgba(244,162,89,0.55)'}`,
                  borderRadius: '10px',
                  cursor: 'pointer',
                  // expanded tap area
                  padding: '12px',
                  boxSizing: 'border-box',
                  transition: 'border-color 0.2s, box-shadow 0.2s',
                  boxShadow: isSelected
                    ? '0 0 0 4px rgba(244,162,89,0.22), inset 0 0 20px rgba(244,162,89,0.08)'
                    : 'none',
                  zIndex: 10,
                }}
              >
                {/* Top-left label */}
                <div
                  style={{
                    position: 'absolute',
                    top: '-26px',
                    left: '-2px',
                    background: 'rgba(0,0,0,0.78)',
                    backdropFilter: 'blur(6px)',
                    padding: '3px 10px',
                    borderRadius: '8px',
                    display: 'flex',
                    alignItems: 'center',
                    gap: '6px',
                    whiteSpace: 'nowrap',
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
                  <span
                    style={{
                      color: '#9AA0A6',
                      fontSize: '10px',
                      fontFamily: R,
                    }}
                  >
                    {Math.round(box.confidence * 100)}%
                  </span>
                </div>

                {/* Selection indicator icon */}
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
                    background: isSelected ? '#F4A259' : 'rgba(30,33,40,0.8)',
                    border: isSelected ? 'none' : '1.5px solid rgba(244,162,89,0.5)',
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

      {/* Bottom Action Buttons */}
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
          disabled={selectedFace === null}
          style={{
            width: '100%',
            height: '52px',
            background:
              selectedFace !== null
                ? 'linear-gradient(135deg, #F4A259 0%, #FFB86C 100%)'
                : '#1e2029',
            border: 'none',
            borderRadius: '16px',
            color: selectedFace !== null ? '#0F1115' : '#4a4f5a',
            fontSize: '15px',
            fontWeight: 600,
            fontFamily: R,
            cursor: selectedFace !== null ? 'pointer' : 'not-allowed',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            gap: '8px',
            transition: 'background 0.25s, color 0.25s',
            boxShadow:
              selectedFace !== null
                ? '0 4px 16px rgba(244,162,89,0.28)'
                : 'none',
          }}
        >
          <CheckCircleIcon style={{ fontSize: '20px' }} />
          Confirm Face Selection
        </button>

        <button
          onClick={onBack}
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
          <HomeIcon style={{ fontSize: '20px', color: '#9AA0A6' }} />
          Back to Home
        </button>
      </div>
    </div>
  );
}
