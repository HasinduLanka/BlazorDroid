using System;
using Android.App;
using Android.OS;
using Android.Provider;
using Android.Runtime;
using Android.Support.Design.Widget;
using Android.Support.V7.App;
using Android.Views;
using Android.Webkit;
using Android.Widget;
using Xamarin.Essentials;

namespace BlazorDroid.Droid
{
    [Activity(Label = "@string/app_name", Theme = "@style/AppTheme.NoActionBar", MainLauncher = true)]
    public class MainActivity : AppCompatActivity
    {
        private static WebView web;// WebView In App
        protected override void OnCreate(Bundle savedInstanceState)
        {
            base.OnCreate(savedInstanceState);
            Xamarin.Essentials.Platform.Init(this, savedInstanceState);
            SetContentView(Resource.Layout.activity_main);

            Android.Support.V7.Widget.Toolbar toolbar = FindViewById<Android.Support.V7.Widget.Toolbar>(Resource.Id.toolbar);
            SetSupportActionBar(toolbar);

            new System.Threading.Thread(new System.Threading.ThreadStart(StartServer)) { Name = "ServerStart" }.Start();
            System.Threading.Thread.Sleep(200);
            web = FindViewById<WebView>(Resource.Id.webView1);

            //LaunchInBrowser(); // Open In Default Web Browser
            LaunchInApp();//Open In Application
        }
        private static void LaunchInBrowser()
        {
            Xamarin.Essentials.Browser.OpenAsync("http://127.0.0.1:6961", new BrowserLaunchOptions() { TitleMode = BrowserTitleMode.Hide });
        }
        private static void LaunchInApp()
        {
            if (web != null)
            {
                web.LoadUrl("http://127.0.0.1:6961");
            }
        }
        private void StartServer()
        {
            ServerInvoker invoker = new ServerInvoker();
            invoker.Start(Assets);
        }
        public override bool OnCreateOptionsMenu(IMenu menu)
        {
            MenuInflater.Inflate(Resource.Menu.menu_main, menu);
            return true;
        }
        public override bool OnOptionsItemSelected(IMenuItem item)
        {
            int id = item.ItemId;
            if (id == Resource.Id.action_opb)
            {
                Snackbar.Make(CurrentFocus, "Opening In Browser...", Snackbar.LengthLong).SetAction("Action", (Android.Views.View.IOnClickListener)null).Show();
                LaunchInBrowser();
                return true;
            }
            if (id == Resource.Id.action_ref)
            {
                if (web != null)
                {
                    Snackbar.Make(CurrentFocus, "Refreshing...", Snackbar.LengthLong).SetAction("Action", (Android.Views.View.IOnClickListener)null).Show();
                    web.Reload();
                }
                return true;
            }
            return base.OnOptionsItemSelected(item);
        }
        public override void OnRequestPermissionsResult(int requestCode, string[] permissions, [GeneratedEnum] Android.Content.PM.Permission[] grantResults)
        {
            Xamarin.Essentials.Platform.OnRequestPermissionsResult(requestCode, permissions, grantResults);
            base.OnRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}

