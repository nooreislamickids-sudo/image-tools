import React from 'react';
import { ShieldCheck, Sparkles, Layers, Image as ImageIcon } from 'lucide-react';
import type { ToolCategory } from '../types.ts';

interface NavbarProps {
  currentCategory: ToolCategory;
  onSelectCategory: (cat: ToolCategory) => void;
  searchQuery: string;
  onSearchChange: (q: string) => void;
  onHomeClick: () => void;
  activeToolName?: string;
}

export const Navbar: React.FC<NavbarProps> = ({
  currentCategory,
  onSelectCategory,
  searchQuery,
  onSearchChange,
  onHomeClick,
  activeToolName,
}) => {
  const categories: { id: ToolCategory; label: string }[] = [
    { id: 'all', label: 'All Tools' },
    { id: 'core', label: 'Core Editing' },
    { id: 'convert', label: 'Convert' },
    { id: 'adjust', label: 'Adjust & Filter' },
    { id: 'creative', label: 'Creative & Effects' },
    { id: 'utilities', label: 'Utilities' },
  ];

  return (
    <header className="sticky top-0 z-40 bg-neutral-900/90 backdrop-blur-md border-b border-neutral-800">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-16 gap-4">
          {/* Logo */}
          <div 
            id="nav-logo"
            onClick={onHomeClick}
            className="flex items-center gap-3 cursor-pointer group select-none"
          >
            <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-indigo-600 to-violet-500 flex items-center justify-center text-white shadow-lg shadow-indigo-500/20 group-hover:scale-105 transition-transform">
              <ImageIcon className="w-5 h-5" />
            </div>
            <div>
              <div className="flex items-center gap-2">
                <span className="font-bold text-lg text-white tracking-tight">Image Tools</span>
                <span className="hidden sm:inline-flex items-center gap-1 text-[10px] uppercase font-semibold px-2 py-0.5 rounded-full bg-indigo-500/10 text-indigo-400 border border-indigo-500/20">
                  <Sparkles className="w-2.5 h-2.5" /> Web Pro
                </span>
              </div>
              <p className="text-xs text-neutral-400 hidden sm:block">Client-Side Image Studio</p>
            </div>
          </div>

          {/* Active breadcrumb or Search */}
          <div className="flex-1 max-w-md">
            {activeToolName ? (
              <div className="flex items-center gap-2 text-sm">
                <button
                  onClick={onHomeClick}
                  className="text-neutral-400 hover:text-white transition-colors cursor-pointer"
                >
                  All Tools
                </button>
                <span className="text-neutral-600">/</span>
                <span className="font-semibold text-indigo-400">{activeToolName}</span>
              </div>
            ) : (
              <div className="relative">
                <input
                  id="nav-search-input"
                  type="text"
                  placeholder="Search 20+ image tools (compress, resize, crop, convert...)"
                  value={searchQuery}
                  onChange={(e) => onSearchChange(e.target.value)}
                  className="w-full bg-neutral-800/80 border border-neutral-700/80 rounded-lg px-3.5 py-1.5 text-sm text-neutral-200 placeholder-neutral-500 focus:outline-none focus:border-indigo-500 focus:ring-1 focus:ring-indigo-500 transition-all"
                />
              </div>
            )}
          </div>

          {/* Privacy badge */}
          <div className="hidden md:flex items-center gap-2 text-xs font-medium text-emerald-400 bg-emerald-950/40 border border-emerald-800/40 px-3 py-1.5 rounded-lg">
            <ShieldCheck className="w-4 h-4 text-emerald-400" />
            <span>100% Client-Side & Private</span>
          </div>
        </div>

        {/* Categories Bar (only when on Home) */}
        {!activeToolName && (
          <div className="flex items-center gap-1 overflow-x-auto py-2.5 scrollbar-none border-t border-neutral-800/60">
            {categories.map((cat) => {
              const active = currentCategory === cat.id;
              return (
                <button
                  key={cat.id}
                  id={`cat-btn-${cat.id}`}
                  onClick={() => onSelectCategory(cat.id)}
                  className={`px-3.5 py-1 text-xs font-medium rounded-full transition-all whitespace-nowrap cursor-pointer ${
                    active
                      ? 'bg-indigo-600 text-white shadow-sm shadow-indigo-600/30'
                      : 'text-neutral-400 hover:text-neutral-200 hover:bg-neutral-800'
                  }`}
                >
                  {cat.label}
                </button>
              );
            })}
          </div>
        )}
      </div>
    </header>
  );
};
