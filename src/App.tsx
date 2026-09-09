import React, { useState, useEffect } from 'react';
import { Navbar } from './components/Navbar.tsx';
import { ToolGrid } from './components/ToolGrid.tsx';
import { ToolWorkspace } from './components/ToolWorkspace.tsx';
import { ALL_TOOLS } from './utils/toolsData.ts';
import type { ToolCategory } from './types.ts';

export const App: React.FC = () => {
  const [selectedCategory, setSelectedCategory] = useState<ToolCategory>('all');
  const [searchQuery, setSearchQuery] = useState<string>('');
  const [activeToolId, setActiveToolId] = useState<string | null>(null);
  const [currentView, setCurrentView] = useState<'home' | 'privacy' | 'terms' | 'about'>('home');

  useEffect(() => {
    const path = window.location.pathname;
    if (path === '/privacy-policy') setCurrentView('privacy');
    else if (path === '/terms') setCurrentView('terms');
    else if (path === '/about') setCurrentView('about');
    else setCurrentView('home');
  }, []);

  const handleNavClick = (view: 'home' | 'privacy' | 'terms' | 'about', e?: React.MouseEvent) => {
    if (e) e.preventDefault();
    setCurrentView(view);
    setActiveToolId(null);
    if (view === 'home') {
      window.history.pushState({}, '', '/');
    } else if (view === 'privacy') {
      window.history.pushState({}, '', '/privacy-policy');
    } else if (view === 'terms') {
      window.history.pushState({}, '', '/terms');
    } else if (view === 'about') {
      window.history.pushState({}, '', '/about');
    }
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  const activeTool = activeToolId ? ALL_TOOLS.find((t) => t.id === activeToolId) : null;

  return (
    <div className="min-h-screen bg-neutral-950 text-neutral-100 flex flex-col font-sans selection:bg-indigo-600/30 selection:text-indigo-200">
      <Navbar
        currentCategory={selectedCategory}
        onSelectCategory={(cat) => {
          setSelectedCategory(cat);
          setActiveToolId(null);
          setCurrentView('home');
        }}
        searchQuery={searchQuery}
        onSearchChange={setSearchQuery}
        onHomeClick={() => {
          setActiveToolId(null);
          setSearchQuery('');
          setCurrentView('home');
        }}
        activeToolName={activeTool?.name}
      />

      <main className="flex-1">
        {currentView === 'privacy' ? (
          <div className="max-w-4xl mx-auto px-4 py-12 space-y-6">
            <h1 className="text-3xl font-bold text-neutral-100">Privacy Policy</h1>
            <p className="text-neutral-400 text-sm">Last updated: September 2026</p>
            <div className="space-y-4 text-neutral-300 text-sm leading-relaxed">
              <p>Welcome to Image Tools (imagetoolshop.shop). Your privacy is critically important to us. This Privacy Policy document outlines the types of information we collect and how we use it.</p>
              <h2 className="text-xl font-semibold text-neutral-200 pt-4">1. Client-Side Processing & Data Security</h2>
              <p>All image processing tasks—including compression, resizing, cropping, converting, and watermarking—are performed 100% locally inside your web browser via client-side JavaScript and hardware-accelerated canvas. <strong>We do not upload, store, or transmit your images or personal files to any external servers.</strong></p>
              <h2 className="text-xl font-semibold text-neutral-200 pt-4">2. Google AdSense & Cookies</h2>
              <p>We use third-party vendors, including Google, to serve ads when you visit our website. Google uses cookies, including the DoubleClick cookie, to enable ads based on users' visits to our site and other sites on the Internet. Users may opt out of personalized advertising by visiting Ads Settings.</p>
              <h2 className="text-xl font-semibold text-neutral-200 pt-4">3. Contact Us</h2>
              <p>If you have any questions or require more information about our Privacy Policy, please feel free to reach out to us through our website.</p>
            </div>
            <button onClick={(e) => handleNavClick('home', e)} className="mt-8 px-6 py-2 bg-indigo-600 hover:bg-indigo-500 text-white rounded-lg text-sm font-medium transition-colors">
              Back to Home
            </button>
          </div>
        ) : currentView === 'terms' ? (
          <div className="max-w-4xl mx-auto px-4 py-12 space-y-6">
            <h1 className="text-3xl font-bold text-neutral-100">Terms & Conditions</h1>
            <p className="text-neutral-400 text-sm">Last updated: September 2026</p>
            <div className="space-y-4 text-neutral-300 text-sm leading-relaxed">
              <p>By accessing and using Image Tools at imagetoolshop.shop, you accept and agree to be bound by the terms and provision of this agreement.</p>
              <h2 className="text-xl font-semibold text-neutral-200 pt-4">1. Use License</h2>
              <p>Permission is granted to temporarily use Image Tools for personal, non-commercial transitory viewing and utility purposes. All tools are provided free of charge "as is" without warranties of any kind.</p>
              <h2 className="text-xl font-semibold text-neutral-200 pt-4">2. User Responsibility</h2>
              <p>Since all processing happens locally on your device, you are solely responsible for the images and media you choose to edit, compress, or manipulate through our interface.</p>
              <h2 className="text-xl font-semibold text-neutral-200 pt-4">3. Changes to Terms</h2>
              <p>We reserve the right to modify these terms at any time. Continued use of the platform constitutes your agreement to such changes.</p>
            </div>
            <button onClick={(e) => handleNavClick('home', e)} className="mt-8 px-6 py-2 bg-indigo-600 hover:bg-indigo-500 text-white rounded-lg text-sm font-medium transition-colors">
              Back to Home
            </button>
          </div>
        ) : currentView === 'about' ? (
          <div className="max-w-4xl mx-auto px-4 py-12 space-y-6">
            <h1 className="text-3xl font-bold text-neutral-100">About Us</h1>
            <p className="text-neutral-400 text-sm">Welcome to Image Tools Studio</p>
            <div className="space-y-4 text-neutral-300 text-sm leading-relaxed">
              <p>Image Tools (imagetoolshop.shop) is a comprehensive, high-performance web suite designed to give creators, developers, and everyday users instant access to professional image utilities directly inside their browsers.</p>
              <h2 className="text-xl font-semibold text-neutral-200 pt-4">Our Mission</h2>
              <p>Our goal is to provide fast, secure, and private browser-based utilities that require zero software installation and guarantee complete file security through local client-side execution.</p>
            </div>
            <button onClick={(e) => handleNavClick('home', e)} className="mt-8 px-6 py-2 bg-indigo-600 hover:bg-indigo-500 text-white rounded-lg text-sm font-medium transition-colors">
              Back to Home
            </button>
          </div>
        ) : activeTool ? (
          <ToolWorkspace
            tool={activeTool}
            onBack={() => setActiveToolId(null)}
          />
        ) : (
          <ToolGrid
            tools={ALL_TOOLS}
            selectedCategory={selectedCategory}
            onSelectTool={(id) => setActiveToolId(id)}
            searchQuery={searchQuery}
          />
        )}
      </main>

      <footer className="mt-auto border-t border-neutral-900 bg-neutral-950 py-8 text-center text-xs text-neutral-500">
        <div className="max-w-7xl mx-auto px-4 flex flex-col sm:flex-row items-center justify-between gap-4">
          <p>© 2026 Image Tools (imagetoolshop.shop). Privacy-first, zero-upload client-side processing.</p>
          <div className="flex items-center gap-6 text-neutral-400 font-medium">
            <a href="/privacy-policy" onClick={(e) => handleNavClick('privacy', e)} className="hover:text-indigo-400 transition-colors cursor-pointer">Privacy Policy</a>
            <span>•</span>
            <a href="/terms" onClick={(e) => handleNavClick('terms', e)} className="hover:text-indigo-400 transition-colors cursor-pointer">Terms & Conditions</a>
            <span>•</span>
            <a href="/about" onClick={(e) => handleNavClick('about', e)} className="hover:text-indigo-400 transition-colors cursor-pointer">About Us</a>
          </div>
        </div>
      </footer>
    </div>
  );
};
