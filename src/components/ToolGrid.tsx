import React from 'react';
import {
  Minimize2,
  Maximize2,
  Crop,
  RotateCw,
  RefreshCw,
  Code,
  FileCode2,
  Sliders,
  Palette,
  Droplet,
  Zap,
  Scissors,
  Stamp,
  Smile,
  LayoutGrid,
  Grid,
  Pipette,
  Info,
  QrCode,
  ArrowRight,
  ShieldCheck,
  Zap as FastIcon,
  Sparkles,
} from 'lucide-react';
import type { ImageToolDefinition, ToolCategory } from '../types.ts';

interface ToolGridProps {
  tools: ImageToolDefinition[];
  selectedCategory: ToolCategory;
  onSelectTool: (toolId: string) => void;
  searchQuery: string;
}

const ICON_MAP: Record<string, React.ReactNode> = {
  Minimize2: <Minimize2 className="w-6 h-6 text-indigo-400" />,
  Maximize2: <Maximize2 className="w-6 h-6 text-blue-400" />,
  Crop: <Crop className="w-6 h-6 text-emerald-400" />,
  RotateCw: <RotateCw className="w-6 h-6 text-amber-400" />,
  RefreshCw: <RefreshCw className="w-6 h-6 text-cyan-400" />,
  Code: <Code className="w-6 h-6 text-purple-400" />,
  FileCode2: <FileCode2 className="w-6 h-6 text-pink-400" />,
  Sliders: <Sliders className="w-6 h-6 text-teal-400" />,
  Palette: <Palette className="w-6 h-6 text-rose-400" />,
  Droplet: <Droplet className="w-6 h-6 text-sky-400" />,
  Zap: <Zap className="w-6 h-6 text-yellow-400" />,
  Scissors: <Scissors className="w-6 h-6 text-orange-400" />,
  Stamp: <Stamp className="w-6 h-6 text-lime-400" />,
  Smile: <Smile className="w-6 h-6 text-amber-400" />,
  LayoutGrid: <LayoutGrid className="w-6 h-6 text-violet-400" />,
  Grid: <Grid className="w-6 h-6 text-indigo-400" />,
  Pipette: <Pipette className="w-6 h-6 text-fuchsia-400" />,
  Info: <Info className="w-6 h-6 text-cyan-400" />,
  QrCode: <QrCode className="w-6 h-6 text-emerald-400" />,
};

export const ToolGrid: React.FC<ToolGridProps> = ({
  tools,
  selectedCategory,
  onSelectTool,
  searchQuery,
}) => {
  const filteredTools = tools.filter((tool) => {
    const matchesCategory = selectedCategory === 'all' || tool.category === selectedCategory;
    const matchesSearch =
      tool.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
      tool.description.toLowerCase().includes(searchQuery.toLowerCase()) ||
      tool.category.toLowerCase().includes(searchQuery.toLowerCase());
    return matchesCategory && matchesSearch;
  });

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      {/* Top Banner / Hero */}
      {!searchQuery && selectedCategory === 'all' && (
        <div className="mb-10 p-6 sm:p-8 rounded-2xl bg-gradient-to-b from-neutral-800/80 to-neutral-900 border border-neutral-800 shadow-xl relative overflow-hidden">
          <div className="absolute top-0 right-0 -mt-8 -mr-8 w-64 h-64 bg-indigo-600/10 rounded-full blur-3xl pointer-events-none" />
          <div className="max-w-3xl">
            <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-indigo-500/10 text-indigo-400 text-xs font-semibold mb-4 border border-indigo-500/20">
              <Sparkles className="w-3.5 h-3.5" /> High-Performance Browser Image Suite
            </div>
            <h1 className="text-2xl sm:text-4xl font-extrabold text-white tracking-tight leading-tight mb-3">
              Professional Image Tools. Free & In Your Browser.
            </h1>
            <p className="text-sm sm:text-base text-neutral-400 leading-relaxed mb-6">
              Compress, resize, convert, crop, enhance, watermark, and analyze images instantly with zero server uploads. High-resolution canvas rendering engine running 100% on your device.
            </p>
            <div className="flex flex-wrap items-center gap-4 text-xs text-neutral-400">
              <div className="flex items-center gap-1.5 text-emerald-400">
                <ShieldCheck className="w-4 h-4" /> Zero Data Uploads (100% Private)
              </div>
              <div className="flex items-center gap-1.5 text-indigo-400">
                <FastIcon className="w-4 h-4" /> Hardware-Accelerated Canvas
              </div>
              <div className="flex items-center gap-1.5 text-purple-400">
                <Sparkles className="w-4 h-4" /> Batch ZIP Export Support
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Grid of Tools */}
      {filteredTools.length === 0 ? (
        <div className="text-center py-16 bg-neutral-800/30 rounded-2xl border border-neutral-800">
          <p className="text-neutral-400 text-base mb-2">No tools match your search "{searchQuery}"</p>
          <p className="text-xs text-neutral-500">Try searching for compress, resize, format, crop, or filter.</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4 sm:gap-5">
          {filteredTools.map((tool) => (
            <div
              key={tool.id}
              id={`tool-card-${tool.id}`}
              onClick={() => onSelectTool(tool.id)}
              className="group bg-neutral-800/60 hover:bg-neutral-800 border border-neutral-700/60 hover:border-indigo-500/50 rounded-xl p-5 transition-all duration-200 cursor-pointer flex flex-col justify-between shadow-sm hover:shadow-lg hover:shadow-indigo-500/5 relative overflow-hidden"
            >
              <div>
                <div className="flex items-start justify-between mb-3.5">
                  <div className="p-2.5 rounded-lg bg-neutral-900/90 border border-neutral-700/50 group-hover:scale-105 group-hover:border-indigo-500/40 transition-all">
                    {ICON_MAP[tool.iconName] || <Minimize2 className="w-6 h-6 text-indigo-400" />}
                  </div>
                  {tool.badge && (
                    <span className="text-[10px] font-semibold tracking-wide uppercase px-2 py-0.5 rounded bg-indigo-500/10 text-indigo-400 border border-indigo-500/20">
                      {tool.badge}
                    </span>
                  )}
                </div>
                <h3 className="font-semibold text-base text-neutral-100 group-hover:text-indigo-400 transition-colors mb-1.5">
                  {tool.name}
                </h3>
                <p className="text-xs text-neutral-400 leading-relaxed line-clamp-2 mb-4">
                  {tool.description}
                </p>
              </div>

              <div className="pt-3 border-t border-neutral-700/40 flex items-center justify-between text-xs font-medium text-neutral-400 group-hover:text-indigo-300">
                <span>Open Tool</span>
                <ArrowRight className="w-3.5 h-3.5 group-hover:translate-x-1 transition-transform" />
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};
