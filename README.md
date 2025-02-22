import React from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Laptop, Brain, Database, FileCode2, User } from "lucide-react";

const PortfolioHome = () => {
  return (
    <div className="max-w-6xl mx-auto p-4 space-y-8">
      {/* Hero Section */}
      <div className="text-center py-12 space-y-4">
        <h1 className="text-4xl font-bold">Patrick Quinn</h1>
        <h2 className="text-2xl text-gray-600">SNHU Computer Science Capstone</h2>
      </div>

      {/* About Section */}
      <Card className="w-full">
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <User className="w-6 h-6" />
            About
          </CardTitle>
        </CardHeader>
        <CardContent>
          <p className="text-lg leading-relaxed">
            Hi! I'm Patrick Quinn. I'm a senior at SNHU studying computer science, looking to further my career 
            and develop my software engineering skills. This project develops extensive implementations for 
            software design, algorithms, and databases on a previously existing project. The following 3 sections 
            will explain what has been done to this project in order to demonstrate skills that I have learned 
            here in my time at SNHU.
          </p>
        </CardContent>
      </Card>

      {/* Enhancement Sections */}
      <div className="grid md:grid-cols-3 gap-6">
        {/* Enhancement 1 */}
        <Card className="w-full">
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Laptop className="w-6 h-6" />
              Enhancement 1
            </CardTitle>
            <CardDescription>Software Design and Engineering</CardDescription>
          </CardHeader>
          <CardContent>
            <p className="text-gray-600">
              Enhanced Android application with modern Material Design principles, advanced data visualization,
              and refined user interaction patterns. Implemented clean architecture with proper separation of concerns
              and efficient data management.
            </p>
            <a href="#" className="text-blue-600 hover:underline mt-4 inline-block">
              Read More →
            </a>
          </CardContent>
        </Card>

        {/* Enhancement 2 */}
        <Card className="w-full">
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Brain className="w-6 h-6" />
              Enhancement 2
            </CardTitle>
            <CardDescription>Algorithms and Data Structures</CardDescription>
          </CardHeader>
          <CardContent>
            <p className="text-gray-600">
              Implemented efficient data handling for weight entries, optimized data structures for chart 
              visualization, and developed algorithms for weight trend analysis. Enhanced performance while
              maintaining functionality.
            </p>
            <a href="#" className="text-blue-600 hover:underline mt-4 inline-block">
              Read More →
            </a>
          </CardContent>
        </Card>

        {/* Enhancement 3 */}
        <Card className="w-full">
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Database className="w-6 h-6" />
              Enhancement 3
            </CardTitle>
            <CardDescription>Databases</CardDescription>
          </CardHeader>
          <CardContent>
            <p className="text-gray-600">
              Enhanced DatabaseHelper class with robust security measures, implementing secure password 
              hashing, comprehensive input validation, and protection against SQL injection. Optimized
              query performance through database indices.
            </p>
            <a href="#" className="text-blue-600 hover:underline mt-4 inline-block">
              Read More →
            </a>
          </CardContent>
        </Card>
      </div>

      {/* Code Review Section */}
      <Card className="w-full">
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <FileCode2 className="w-6 h-6" />
            Code Review
          </CardTitle>
        </CardHeader>
        <CardContent>
          <p className="text-gray-600">
            A comprehensive code review of the original project design is available, analyzing the initial
            implementation and identifying areas for enhancement.
          </p>
          <a href="#" className="text-blue-600 hover:underline mt-4 inline-block">
            View Code Review →
          </a>
        </CardContent>
      </Card>
    </div>
  );
};

export default PortfolioHome;

