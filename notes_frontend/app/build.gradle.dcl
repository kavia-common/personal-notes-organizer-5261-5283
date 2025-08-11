androidApplication {
    namespace = "org.example.app"

    dependencies {
        implementation("androidx.appcompat:appcompat:1.7.0")
        implementation("com.google.android.material:material:1.12.0")
        implementation("androidx.recyclerview:recyclerview:1.3.2")
        implementation("androidx.constraintlayout:constraintlayout:2.1.4")
        implementation("androidx.coordinatorlayout:coordinatorlayout:1.2.0")
        // Core KTX is useful but optional; keeping footprint minimal
        implementation("androidx.core:core-ktx:1.13.1")

    }
    
    testing {
        dependencies {
            implementation("junit:junit:4.13.2")
        }
    }
}
