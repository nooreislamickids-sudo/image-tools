import React, { useState } from 'react';
import { Navbar } from './components/Navbar.tsx';
import { ToolGrid } from './components/ToolGrid.tsx';
import { ToolWorkspace } from './components/ToolWorkspace.tsx';
import { ALL_TOOLS } from './utils/toolsData.ts';
import type { ToolCategory } from './types.ts';

export const App: React.FC = () => {
  const [selectedCategory, setSelectedCategory] = useState<ToolCategory>('all');
  const [searchQuery, setSearchQuery] = useState<string>('');
  const [activeToolId, setActiveToolId] = useState<string | null>(null);

  const activeTool = activeToolId ? ALL_TOOLS.find((t) => t.id === activeToolId) : null;

  return (
    <div className="min-h-screen bg-neutral-950 text-neutral-100 flex flex-col font-sans selection:bg-indigo-600/30 selection:text-indigo-200">
      <Navbar
        currentCategory={selectedCategory}
        onSelectCategory={(cat) => {
          setSelectedCategory(cat);
          setActiveToolId(null);
        }}
        searchQuery={searchQuery}
        onSearchChange={setSearchQuery}
        onHomeClick={() => {
          setActiveToolId(null);
          setSearchQuery('');
        }}
        activeToolName={activeTool?.name}
      />

      <main className="flex-1">
        {activeTool ? (
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

      <footer className="mt-auto border-t border-neutral-900 bg-neutral-950 py-6 text-center text-xs text-neutral-500">
        <div className="max-w-7xl mx-auto px-4 flex flex-col sm:flex-row items-center justify-between gap-4">
          <p>© 2026 Image Tools Studio. Privacy-first, zero-upload client-side processing.</p>
          <div className="flex items-center gap-4 text-neutral-400">
            <span>Hardware Accelerated Canvas</span>
            <span>•</span>
            <span>20+ Pro Utilities</span>
            <span>•</span>
            <span>Client-Side Security</span>
          </div>
        </div>
      </footer>
    </div>
  );
};
