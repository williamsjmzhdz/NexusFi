import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import {
  LayoutDashboard,
  TrendingUp,
  TrendingDown,
  Tag,
  LogOut,
} from 'lucide-react';

const navItems = [
  { to: '/dashboard', icon: LayoutDashboard, label: 'Dashboard' },
  { to: '/income', icon: TrendingUp, label: 'Income' },
  { to: '/expenses', icon: TrendingDown, label: 'Expenses' },
  { to: '/categories', icon: Tag, label: 'Categories' },
];

function DashboardLayout() {
  const navigate = useNavigate();
  const email = localStorage.getItem('email') ?? '';
  const firstName = localStorage.getItem('firstName') ?? email;

  function handleLogout() {
    localStorage.removeItem('token');
    localStorage.removeItem('email');
    localStorage.removeItem('firstName');
    navigate('/login');
  }

  return (
    <div className="flex min-h-screen bg-slate-50">

      {/* Sidebar */}
      <aside className="w-60 bg-white border-r border-slate-200 flex flex-col shrink-0">

        {/* Brand */}
        <div className="px-6 py-5 border-b border-slate-100">
          <span className="text-xl font-bold tracking-tight"
            style={{ background: 'linear-gradient(135deg, #4F46E5, #818CF8)', WebkitBackgroundClip: 'text', WebkitTextFillColor: 'transparent' }}>
            NexusFi
          </span>
        </div>

        {/* Navigation */}
        <nav className="flex-1 px-3 py-4 space-y-1">
          {navItems.map(({ to, icon: Icon, label }) => (
            <NavLink
              key={to}
              to={to}
              end={to === '/dashboard'}
              className={({ isActive }) =>
                `flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition ${
                  isActive
                    ? 'bg-indigo-50 text-indigo-600'
                    : 'text-slate-500 hover:bg-slate-50 hover:text-slate-800'
                }`
              }
            >
              <Icon size={18} />
              {label}
            </NavLink>
          ))}
        </nav>

        {/* Logout */}
        <div className="px-3 py-4 border-t border-slate-100">
          <div className="px-3 py-2 mb-1">
            <p className="text-xs text-slate-400 truncate">{email}</p>
          </div>
          <button
            onClick={handleLogout}
            className="flex items-center gap-3 w-full px-3 py-2.5 rounded-lg text-sm font-medium text-slate-500 hover:bg-rose-50 hover:text-rose-600 transition"
          >
            <LogOut size={18} />
            Sign out
          </button>
        </div>

      </aside>

      {/* Main content */}
      <div className="flex-1 flex flex-col min-w-0">

        {/* Topbar */}
        <header className="h-14 bg-white border-b border-slate-200 flex items-center px-6 shrink-0">
          <p className="text-sm text-slate-400">
            Welcome back, <span className="font-medium text-slate-700">{firstName}</span>
          </p>
        </header>

        {/* Page content */}
        <main className="flex-1 p-6">
          <Outlet />
        </main>

      </div>
    </div>
  );
}

export default DashboardLayout;
