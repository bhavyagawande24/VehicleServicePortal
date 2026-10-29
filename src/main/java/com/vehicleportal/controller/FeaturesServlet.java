package com.vehicleportal.controller;

import com.vehicleportal.util.DBConnection;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import jakarta.servlet.*;
import java.io.*;
import java.sql.*;

@WebServlet("/features/*")
public class FeaturesServlet extends HttpServlet {
    private String esc(String s) {
        if (s == null) return "";
        return s.replace("&","&amp;").replace("<","&lt;")
                .replace(">","&gt;").replace("\"","&quot;")
                .replace("'","&#39;");
    }

    private int user(HttpServletRequest r) {
        Object id = r.getSession(false) == null ? null :
                r.getSession(false).getAttribute("userId");
        return id instanceof Integer ? (Integer) id : -1;
    }

    private boolean admin(HttpServletRequest r) {
        return r.getSession(false) != null &&
                r.getSession(false).getAttribute("adminId") != null;
    }

    private double price(String service) {
        if (service == null) return 0;
        switch (service) {
            case "Oil Change": return 1499;
            case "General Service": return 2499;
            case "Brake Service": return 1999;
            case "Engine Service": return 4999;
            case "AC Service": return 1799;
            case "Full Service": return 3999;
            default: return 0;
        }
    }

    private void schema() {
        try (Connection c = DBConnection.getConnection();
             Statement s = c.createStatement()) {
            try {
                s.executeUpdate("ALTER TABLE bookings ADD COLUMN estimated_cost DECIMAL(10,2) NULL");
            } catch (SQLException ignored) { }
        } catch (SQLException e) {
            getServletContext().log("Could not prepare estimated_cost column", e);
        }
    }

    private void page(HttpServletResponse response, String title, String body)
            throws IOException {
        response.setContentType("text/html;charset=UTF-8");
        response.getWriter().println("""
        <!doctype html><html lang="en"><head><meta charset="UTF-8">
        <meta name="viewport" content="width=device-width,initial-scale=1">
        <title>AUTOLUX | """ + esc(title) + """
        </title><style>
        *{box-sizing:border-box}body{margin:0;background:#10110e;color:#f4f4f0;
        font:15px Arial,sans-serif;padding:24px}
        .wrap{max-width:1150px;margin:auto}.brand{color:#c6ff00;font-size:28px;
        font-weight:900;letter-spacing:3px}.nav{display:flex;gap:10px;flex-wrap:wrap;
        margin:22px 0}.nav a,button{background:#c6ff00;color:#151710;padding:11px 15px;
        border:0;border-radius:8px;font-weight:bold;text-decoration:none;cursor:pointer}
        .card{background:#1c1e19;border:1px solid #34372e;border-radius:14px;
        padding:20px;margin:14px 0;overflow-x:auto}.stats{display:grid;
        grid-template-columns:repeat(auto-fit,minmax(160px,1fr));gap:12px}
        .num{font-size:30px;color:#c6ff00;font-weight:bold;margin-top:12px}
        h1,h2{margin-top:8px}h2{color:#c6ff00}table{width:100%;border-collapse:collapse;
        min-width:650px}td,th{text-align:left;padding:12px;border-bottom:1px solid #383a32}
        th{color:#c6ff00}input,select{padding:10px;background:#11130f;color:white;
        border:1px solid #555a4a;border-radius:7px;margin:4px;max-width:100%}
        .muted{color:#b8bbaE}.danger{background:#ff6464}.tag{color:#c6ff00}
        form{display:inline-block;margin:3px}a{color:#c6ff00}
        @media(max-width:600px){body{padding:13px}.card{padding:13px}}
        </style></head><body><div class="wrap">
        <div class="brand">AUTOLUX</div>
        <div id="notice" style="display:none;padding:12px 16px;margin:15px 0;border-radius:8px;background:#262b18;border:1px solid #c6ff00;color:#c6ff00"></div>
        <script>
        (function(){
          const p=new URLSearchParams(location.search);
          const m=p.get('msg');
          const n=document.getElementById('notice');
          if(m){n.textContent=m;n.style.display='block';}
        })();
        </script>
        <div class="nav">
        <a href="dashboard">Dashboard</a><a href="bookings">My Bookings</a>
        <a href="garage">My Garage</a><a href="../dashboard.html">Main Dashboard</a>
        <a href="../logout">Logout</a></div>
        """ + body + "</div></body></html>");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        String path = req.getPathInfo();
        if (path == null) path = "/dashboard";
        int uid = user(req);
        boolean isAdmin = admin(req);
        if (uid < 0 && !isAdmin) {
            res.sendRedirect(req.getContextPath() + "/login.html");
            return;
        }
        schema();
        try (Connection c = DBConnection.getConnection()) {
            String msg=req.getParameter("msg");

            if (path.equals("/admin")) {
                if (!isAdmin) { res.sendError(403, "Admin access required"); return; }
                StringBuilder b = new StringBuilder("<h1>Admin Booking Management</h1><div class='card'><form><input name='q' placeholder='Search service, status or booking ID' value='"+esc(req.getParameter("q"))+"'><button>Search</button></form><table><tr><th>ID</th><th>User</th><th>Service</th><th>Date</th><th>Status</th><th>Update</th></tr>");
                String q = req.getParameter("q");
                String sql = "SELECT b.booking_id,u.name,b.service_type,b.booking_date,b.status FROM bookings b JOIN users u ON b.user_id=u.id WHERE (? IS NULL OR CAST(b.booking_id AS CHAR) LIKE ? OR b.service_type LIKE ? OR b.status LIKE ? OR u.name LIKE ?) ORDER BY b.booking_id DESC";
                try (PreparedStatement p = c.prepareStatement(sql)) {
                    p.setString(1, q); String like = "%" + (q == null ? "" : q) + "%";
                    for (int i=2;i<=5;i++) p.setString(i,like);
                    try (ResultSet r=p.executeQuery()) {
                        while(r.next()) b.append("<tr><td>").append(r.getInt(1)).append("</td><td>").append(esc(r.getString(2))).append("</td><td>").append(esc(r.getString(3))).append("</td><td>").append(r.getDate(4)).append("</td><td>").append(esc(r.getString(5))).append("</td><td><form method='post' action='").append(req.getContextPath()).append("/features/action'><input type='hidden' name='id' value='").append(r.getInt(1)).append("'><select name='status'><option>Pending</option><option>Confirmed</option><option>In Progress</option><option>Completed</option><option>Cancelled</option></select><button>Save</button></form></td></tr>");
                    }
                }
                b.append("</table></div>");
                page(res,"Admin",b.toString()); return;
            }

            if (path.equals("/garage")) {
                StringBuilder b=new StringBuilder("<h1>My Garage</h1><div class='card'><h2>Add a vehicle</h2><form method='post' action='").append(req.getContextPath()).append("/features/action'><input type='hidden' name='action' value='addVehicle'><input name='number' placeholder='Vehicle registration' required><input name='type' placeholder='Type (Car/Bike)' required><input name='model' placeholder='Model' required><button>Add Vehicle</button></form></div><div class='card'><table><tr><th>Registration</th><th>Type</th><th>Model</th><th>Service history</th><th>Action</th></tr>");
                try(PreparedStatement p=c.prepareStatement("SELECT vehicle_id,vehicle_number,vehicle_type,model FROM vehicles WHERE user_id=? ORDER BY vehicle_id DESC")) {
                    p.setInt(1,uid);
                    try(ResultSet r=p.executeQuery()) {
                        while(r.next()) {
                            int vid=r.getInt(1);
                            b.append("<tr><td>").append(esc(r.getString(2))).append("</td><td>").append(esc(r.getString(3))).append("</td><td>").append(esc(r.getString(4))).append("</td><td><a href='bookings?vehicle=").append(vid).append("'>View bookings</a></td><td><details><summary>Edit</summary><form method='post' action='").append(req.getContextPath()).append("/features/action'><input type='hidden' name='action' value='editVehicle'><input type='hidden' name='id' value='").append(vid).append("'><input name='number' required value='").append(esc(r.getString(2))).append("' placeholder='Registration'><input name='type' required value='").append(esc(r.getString(3))).append("' placeholder='Type'><input name='model' required value='").append(esc(r.getString(4))).append("' placeholder='Model'><button>Save</button></form></details><form method='post' action='").append(req.getContextPath()).append("/features/action' onsubmit=\"return confirm('Delete this vehicle? Vehicles with bookings cannot be deleted.')\"><input type='hidden' name='action' value='deleteVehicle'><input type='hidden' name='id' value='").append(vid).append("'><button class='danger'>Delete</button></form></td></tr>");
                        }
                    }
                }
                b.append("</table></div>");
                page(res,"My Garage",b.toString()); return;
            }

            if (path.equals("/bookings")) {
                String date = req.getParameter("date");

                
                String status=req.getParameter("status"), vehicle=req.getParameter("vehicle");
                String q=req.getParameter("q");
                StringBuilder b=new StringBuilder("<h1>My Bookings & Service Tracker</h1><div class='card'><form><input name='q' placeholder='Search service or booking ID' value='"+esc(q)+"'><select name='status'><option value=''>All statuses</option><option>Pending</option><option>Confirmed</option><option>Approved</option><option>In Progress</option><option>Completed</option><option>Cancelled</option></select><input name='vehicle' placeholder='Vehicle ID' value='"+esc(vehicle)+"'><input type='date' name='date' value='"+esc(date)+"'><button>Filter</button></form></div><div class='card'><table><tr><th>ID</th><th>Vehicle</th><th>Service</th><th>Date</th><th>Status</th><th>Estimate</th><th>Action</th></tr>");
                
                String sql="SELECT b.booking_id,v.vehicle_number,b.service_type,b.booking_date,b.status,b.estimated_cost,b.vehicle_id FROM bookings b JOIN vehicles v ON b.vehicle_id=v.vehicle_id WHERE b.user_id=? AND (? IS NULL OR b.status=?) AND (? IS NULL OR b.vehicle_id=?) AND (? IS NULL OR b.service_type LIKE ? OR CAST(b.booking_id AS CHAR) LIKE ?) AND (? IS NULL OR b.booking_date=?) ORDER BY b.booking_id DESC";
                try(PreparedStatement p=c.prepareStatement(sql)) {
                    p.setInt(1,uid); p.setString(2,status==null||status.isBlank()?null:status); p.setString(3,status);
                    p.setString(4,vehicle==null||vehicle.isBlank()?null:vehicle);
                    p.setInt(5,vehicle!=null&&vehicle.matches("\\\\d+")?Integer.parseInt(vehicle): -1);
                    p.setString(6,q==null||q.isBlank()?null:q); String like="%"+(q==null?"":q)+"%";
                    p.setString(7,like); p.setString(8,like);
                    p.setString(9,date==null||date.isBlank()?null:date);
                    p.setString(10,date==null||date.isBlank()?null:date);
                    try(ResultSet r=p.executeQuery()) {
                        while(r.next()) {
                            int id=r.getInt(1); String service=r.getString(3); double cost=price(service);
                            if(r.getObject(6)==null) try(PreparedStatement u=c.prepareStatement("UPDATE bookings SET estimated_cost=? WHERE booking_id=? AND user_id=? AND estimated_cost IS NULL")){u.setDouble(1,cost);u.setInt(2,id);u.setInt(3,uid);u.executeUpdate();}
                            String st=r.getString(5);
                            if ("Approved".equalsIgnoreCase(st)) st = "Confirmed";
                            b.append("<tr><td>").append(id).append("</td><td>").append(esc(r.getString(2))).append("</td><td>").append(esc(service)).append("</td><td>").append(r.getDate(4)).append("</td><td><span class='tag'>").append(esc(st)).append("</span><br><small class='muted'>Pending → Confirmed → In Progress → Completed</small></td><td>₹").append(String.format("%.0f",r.getObject(6)==null?cost:r.getDouble(6))).append("</td><td>");
                            if("Pending".equalsIgnoreCase(st)) b.append("<form method='post' action='").append(req.getContextPath()).append("/features/action' onsubmit=\"return confirm('Cancel this booking?')\"><input type='hidden' name='action' value='cancel'><input type='hidden' name='id' value='").append(id).append("'><button class='danger'>Cancel</button></form>");
                            b.append("</td></tr>");
                        }
                    }
                }
                b.append("</table><p class='muted'>Estimates are indicative only; final cost may vary after inspection.</p></div>");
                page(res,"My Bookings",b.toString()); return;
            }

            StringBuilder b=new StringBuilder("<h1>Your Service Dashboard</h1><p class='muted'>Your AUTOLUX account at a glance.</p><div class='stats'>");
            String[] labels={"My Vehicles","Total Bookings","Pending","Completed"};
            String[] queries={
                "SELECT COUNT(*) FROM vehicles WHERE user_id=?",
                "SELECT COUNT(*) FROM bookings WHERE user_id=?",
                "SELECT COUNT(*) FROM bookings WHERE user_id=? AND status='Pending'",
                "SELECT COUNT(*) FROM bookings WHERE user_id=? AND status='Completed'"
            };
            for(int i=0;i<queries.length;i++){
                try(PreparedStatement p=c.prepareStatement(queries[i])){
                    p.setInt(1,uid);try(ResultSet r=p.executeQuery()){r.next();b.append("<div class='card'><div>").append(labels[i]).append("</div><div class='num'>").append(r.getInt(1)).append("</div></div>");}
                }
            }
            b.append("</div><div class='card'><h2>Service Cost Estimator</h2><p>Select a service to see an indicative estimate.</p><select id='service' onchange='estimate()'><option value=''>Choose service</option><option value='1499'>Oil Change</option><option value='2499'>General Service</option><option value='1999'>Brake Service</option><option value='4999'>Engine Service</option><option value='1799'>AC Service</option><option value='3999'>Full Service</option></select><h2 id='estimate'>Estimated cost: —</h2><p class='muted'>Estimates are indicative; actual charges depend on inspection and parts.</p><script>function estimate(){let v=document.getElementById('service').value;document.getElementById('estimate').textContent='Estimated cost: '+(v?'₹'+v:'—')}</script></div><div class='card'><h2>Quick actions</h2><p><a href='bookings'>Track and search your bookings</a></p><p><a href='garage'>Manage your garage</a></p>");
            if(isAdmin)b.append("<p><a href='admin'>Open Admin Management</a></p>");
            b.append("</div>");
            page(res,"Dashboard",b.toString());
        } catch(Exception e) {
            getServletContext().log("AUTOLUX feature page error",e);
            res.sendError(500,"Unable to load this page. Check the Tomcat logs.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req,HttpServletResponse res)
            throws ServletException,IOException {
        String action=req.getParameter("action");
        int uid=user(req);
        boolean isAdmin=admin(req);
        if(uid<0&&!isAdmin){res.sendRedirect(req.getContextPath()+"/login.html");return;}
        schema();
        try(Connection c=DBConnection.getConnection()){
            if("updateStatus".equals(action) || (isAdmin && req.getParameter("status")!=null && req.getParameter("id")!=null && action==null)){
                if(!isAdmin){res.sendError(403,"Admin access required");return;}
                String status=req.getParameter("status");
                if(!java.util.Arrays.asList("Pending","Confirmed","Approved","In Progress","Completed","Cancelled").contains(status)){res.sendError(400);return;}
                try(PreparedStatement p=c.prepareStatement("UPDATE bookings SET status=? WHERE booking_id=?")){
                    p.setString(1,status);p.setInt(2,Integer.parseInt(req.getParameter("id")));p.executeUpdate();
                }
            } else if("cancel".equals(action)){
                try(PreparedStatement p=c.prepareStatement("UPDATE bookings SET status='Cancelled' WHERE booking_id=? AND user_id=? AND status='Pending'")){
                    p.setInt(1,Integer.parseInt(req.getParameter("id")));p.setInt(2,uid);p.executeUpdate();
                }
            } else if("addVehicle".equals(action)){
                try(PreparedStatement p=c.prepareStatement("INSERT INTO vehicles(user_id,vehicle_number,vehicle_type,model) VALUES(?,?,?,?)")){
                    p.setInt(1,uid);p.setString(2,req.getParameter("number").trim().toUpperCase());p.setString(3,req.getParameter("type").trim());p.setString(4,req.getParameter("model").trim());p.executeUpdate();
                }
            } else if("editVehicle".equals(action)){
                if(uid < 0){res.sendError(403,"User access required");return;}
                try(PreparedStatement p=c.prepareStatement(
                    "UPDATE vehicles SET vehicle_number=?, vehicle_type=?, model=? WHERE vehicle_id=? AND user_id=?")){
                    p.setString(1,req.getParameter("number").trim().toUpperCase());
                    p.setString(2,req.getParameter("type").trim());
                    p.setString(3,req.getParameter("model").trim());
                    p.setInt(4,Integer.parseInt(req.getParameter("id")));
                    p.setInt(5,uid);
                    p.executeUpdate();
                }
            } else if("deleteVehicle".equals(action)){
                try(PreparedStatement check=c.prepareStatement("SELECT COUNT(*) FROM bookings WHERE vehicle_id=?")){
                    check.setInt(1,Integer.parseInt(req.getParameter("id")));
                    try(ResultSet r=check.executeQuery()){r.next();if(r.getInt(1)>0){res.sendRedirect(req.getContextPath()+"/features/garage?msg=Vehicle+has+booking+history+and+cannot+be+deleted");return;}}
                }
                try(PreparedStatement p=c.prepareStatement("DELETE FROM vehicles WHERE vehicle_id=? AND user_id=?")){
                    p.setInt(1,Integer.parseInt(req.getParameter("id")));p.setInt(2,uid);p.executeUpdate();
                }
            }
            String dest=isAdmin?"/features/admin":(("addVehicle".equals(action)||"deleteVehicle".equals(action)||"editVehicle".equals(action))?"/features/garage":"/features/bookings");
            String message;
            if ("addVehicle".equals(action)) message = "Vehicle+added+successfully";
            else if ("editVehicle".equals(action)) message = "Vehicle+updated+successfully";
            else if ("deleteVehicle".equals(action)) message = "Vehicle+deleted+successfully";
            else if ("cancel".equals(action)) message = "Cancellation+requested";
            else if (isAdmin) message = "Booking+status+updated";
            else message = "Action+completed";
            res.sendRedirect(req.getContextPath()+dest+"?msg="+message);
        }catch(Exception e){
            getServletContext().log("AUTOLUX feature action failed",e);
            res.sendError(500,"Action failed. Check the Tomcat logs.");
        }
    }
}
