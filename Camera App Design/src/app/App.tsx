import { useState } from 'react';
import { PhoneFrame } from './components/PhoneFrame';
import { SplashScreen } from './components/screens/SplashScreen';
import { HomeScreen } from './components/screens/HomeScreen';
import { CameraScreen } from './components/screens/CameraScreen';
import { FaceDetectionScreen } from './components/screens/FaceDetectionScreen';
import { EyeDetectionScreen } from './components/screens/EyeDetectionScreen';
import { ResultScreen } from './components/screens/ResultScreen';

export type AppScreen =
  | 'splash'
  | 'home'
  | 'camera'
  | 'face-detection'
  | 'eye-detection'
  | 'result';

interface AppState {
  selectedFace: number | null;
  selectedEye: 'left' | 'right' | null;
}

const SCREEN_LABELS: Record<AppScreen, string> = {
  splash: 'Splash',
  home: 'Home',
  camera: 'Camera',
  'face-detection': 'Face',
  'eye-detection': 'Eye',
  result: 'Result',
};

const SCREEN_ORDER: AppScreen[] = [
  'splash',
  'home',
  'camera',
  'face-detection',
  'eye-detection',
  'result',
];

export default function App() {
  const [screen, setScreen] = useState<AppScreen>('splash');
  const [appState, setAppState] = useState<AppState>({
    selectedFace: null,
    selectedEye: null,
  });

  const navigate = (nextScreen: AppScreen, updates?: Partial<AppState>) => {
    if (updates) setAppState((prev) => ({ ...prev, ...updates }));
    setScreen(nextScreen);
  };

  const activeIndex = SCREEN_ORDER.indexOf(screen);

  return (
    <div
      style={{
        minHeight: '100vh',
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        background:
          'radial-gradient(ellipse at 60% 30%, #1a1f2e 0%, #0d1117 55%, #0a0c0f 100%)',
        fontFamily: "'Roboto', sans-serif",
        padding: '24px 16px',
        gap: '20px',
      }}
    >
      {/* Phone frame */}
      <PhoneFrame>
        {screen === 'splash' && (
          <SplashScreen onComplete={() => navigate('home')} />
        )}

        {screen === 'home' && (
          <HomeScreen
            onCapture={() => navigate('camera')}
            onUpload={() => navigate('face-detection')}
            onLiveCamera={() => navigate('camera')}
          />
        )}

        {screen === 'camera' && (
          <CameraScreen
            onCapture={() => navigate('face-detection')}
            onBack={() => navigate('home')}
          />
        )}

        {screen === 'face-detection' && (
          <FaceDetectionScreen
            selectedFace={appState.selectedFace}
            onSelectFace={(face) =>
              setAppState((prev) => ({ ...prev, selectedFace: face }))
            }
            onConfirm={() => navigate('eye-detection')}
            onBack={() => navigate('home')}
          />
        )}

        {screen === 'eye-detection' && (
          <EyeDetectionScreen
            selectedFace={appState.selectedFace}
            selectedEye={appState.selectedEye}
            onSelectEye={(eye) =>
              setAppState((prev) => ({ ...prev, selectedEye: eye }))
            }
            onConfirm={() => navigate('result')}
            onReselect={() => navigate('face-detection')}
          />
        )}

        {screen === 'result' && (
          <ResultScreen
            selectedFace={appState.selectedFace}
            selectedEye={appState.selectedEye}
            onStartNew={() =>
              navigate('home', { selectedFace: null, selectedEye: null })
            }
            onBack={() => navigate('eye-detection')}
          />
        )}
      </PhoneFrame>

      {/* Screen navigation indicator */}
      <div
        style={{
          display: 'flex',
          alignItems: 'center',
          gap: '6px',
          background: 'rgba(28,31,38,0.85)',
          backdropFilter: 'blur(12px)',
          borderRadius: '40px',
          padding: '10px 16px',
          border: '1px solid rgba(255,255,255,0.07)',
        }}
      >
        {SCREEN_ORDER.map((s, idx) => {
          const isActive = s === screen;
          const isPast = idx < activeIndex;
          return (
            <button
              key={s}
              onClick={() => {
                // Quick-jump for development convenience
                if (s !== 'splash') navigate(s);
              }}
              title={SCREEN_LABELS[s]}
              style={{
                display: 'flex',
                alignItems: 'center',
                gap: '5px',
                background: 'none',
                border: 'none',
                cursor: 'pointer',
                padding: '0 2px',
              }}
            >
              <div
                style={{
                  width: isActive ? '24px' : '8px',
                  height: '8px',
                  borderRadius: '4px',
                  background: isActive
                    ? '#F4A259'
                    : isPast
                    ? 'rgba(244,162,89,0.4)'
                    : 'rgba(255,255,255,0.15)',
                  transition: 'width 0.3s ease, background 0.3s ease',
                }}
              />
            </button>
          );
        })}
        <span
          style={{
            color: '#9AA0A6',
            fontSize: '12px',
            fontFamily: "'Roboto', sans-serif",
            marginLeft: '6px',
          }}
        >
          {SCREEN_LABELS[screen]}
        </span>
      </div>

      {/* Branding */}
      <div
        style={{
          color: 'rgba(154,160,166,0.4)',
          fontSize: '11px',
          fontFamily: "'Roboto', sans-serif",
          letterSpacing: '0.06em',
        }}
      >
        ChakraVue Eye Scanner · Material 3 UI
      </div>
    </div>
  );
}
