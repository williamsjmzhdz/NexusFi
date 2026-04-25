function DashboardPage() {
  return (
    <div>
      <h1 className="text-2xl font-semibold text-slate-900 mb-1">Dashboard</h1>
      <p className="text-sm text-slate-500">Your financial overview</p>

      {/* Balance cards + movements coming next */}
      <div className="mt-8 flex items-center justify-center h-48 rounded-xl border-2 border-dashed border-slate-200">
        <p className="text-sm text-slate-400">Balance cards coming soon</p>
      </div>
    </div>
  );
}

export default DashboardPage;

